package com.example.user.handler;

import com.example.user.entity.IncomingMessage;
import com.example.user.entity.OutgoingMessage;
import com.example.user.entity.ChatMessage;
import com.example.user.repository.ChatMessageRepository;
import com.example.user.service.PresenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {


    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
    // 记录用户分配给了哪个客服: username -> agentUsername
    private static final Map<String, String> USER_AGENT_MAP = new ConcurrentHashMap<>();
    // 反向映射: agentUsername -> username (用于AI回复时归属正确的访客会话)
    private static final Map<String, String> AGENT_USER_MAP = new ConcurrentHashMap<>();
    @Resource
    PresenceService presenceService;
    @Resource
    ObjectMapper objectMapper;
    @Resource
    ChatMessageRepository chatMessageRepository;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 直接从URL解析参数
        String query = session.getUri().getQuery();
        String username = null;
        String role = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    if ("username".equals(kv[0])) username = kv[1];
                    if ("role".equals(kv[0])) role = kv[1];
                }
            }
        }
        // 放到session属性，后续可以直接用
        session.getAttributes().put("username", username);
        session.getAttributes().put("role", role);

        // 通知用户连接成功
        presenceService.addSession(username, role, session, SESSIONS);

        log.info("用户连接: " + username + ", 当前在线: " + SESSIONS.size());


        sendSystemMessage(session, "已连接到服务器，当前身份：" + username + "（" + role + "）");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String username = String.valueOf(session.getAttributes().get("username"));
        String role = String.valueOf(session.getAttributes().get("role"));
        log.info("[WS-RECV] 用户: {}, 身份: {}, 原始报文: {}", username, role, message.getPayload());
        
        if (username == null || role == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        IncomingMessage incomingMessage;
        try {
            incomingMessage = objectMapper.readValue(message.getPayload(), IncomingMessage.class);
        } catch (Exception e) {
            log.error("[WS-ERR] 消息解析失败", e);
            sendSystemMessage(session, "消息格式错误，必须是 JSON");
            return;
        }
        if (incomingMessage.getContent().isBlank() || incomingMessage.getContent() == null) {
            sendSystemMessage(session, "内容不能为空");
            return;
        }

        String to = incomingMessage.getTo();
        log.info("[WS-LOGIC] 目标: [{}], 内容: [{}], 身份: {}", to, incomingMessage.getContent(), role);

        // 1. 优先处理系统指令（转人工、切回 AI），这些指令不存入聊天历史
        if ("SYSTEM".equalsIgnoreCase(to)) {
            if ("REQUEST_HUMAN".equalsIgnoreCase(incomingMessage.getContent())) {
                handleHumanTransfer(username);
                return;
            }
            if ("SWITCH_TO_AI".equalsIgnoreCase(incomingMessage.getContent())) {
                USER_AGENT_MAP.remove(username);
                log.info("用户 {} 已切换回 AI 模式", username);
                sendSystemMessage(session, "已为您切换回智能助理模式");
                return;
            }
        }

        // 只有非指令类的聊天消息才存入数据库 (放在所有特殊逻辑判断之后，统一保存)
        // 这里先不存，等到各个分支里去存，防止重复

        if ((to == null || to.isBlank()) && "CUSTOMER".equalsIgnoreCase(role)) {
            Set<String> onlineAgents = presenceService.listOnlineAgents();
            if (onlineAgents.isEmpty()) {
                sendSystemMessage(session, "当前没有客服在线，请稍后再试。");
                return;
            }
            String next = onlineAgents.iterator().next();
            WebSocketSession agentSession = presenceService.getSession(next);
            if (agentSession != null && agentSession.isOpen()) {
                sendSystemMessage(session, "已为你接入客服：" + next);
                OutgoingMessage outgoingMessage = new OutgoingMessage(username, next, incomingMessage.getContent());
                outgoingMessage.setSenderRole("USER");
                agentSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(outgoingMessage)));
                
                // 保存首次接入客服的消息
                saveChatMessage(username, next, incomingMessage.getContent(), false, incomingMessage.getSessionId(), "USER");
            } else {
                sendSystemMessage(session, "客服暂时不可用，请稍后再试。");
            }
            return;
        }
        if (to == null || to.isBlank()) {
            sendSystemMessage(session, "to 不能为空（除非你是 CUSTOMER 首次发起咨询");
            return;
        }

        // 处理转人工后的聊天消息
        if ("AGENT".equalsIgnoreCase(to)) {
            String agentName = USER_AGENT_MAP.get(username);
            if (agentName == null) {
                Set<String> agents = presenceService.listOnlineAgents();
                if (!agents.isEmpty()) {
                    agentName = agents.iterator().next();
                    USER_AGENT_MAP.put(username, agentName);
                }
            }
            
            if (agentName != null) {
                USER_AGENT_MAP.put(username, agentName);
                AGENT_USER_MAP.put(agentName, username);
                WebSocketSession agentSession = presenceService.getSession(agentName);
                if (agentSession != null && agentSession.isOpen()) {
                    log.info("[WS-FORWARD] 转发人工消息给客服: {}, 来源: {}", agentName, username);
                    try {
                        OutgoingMessage msg = new OutgoingMessage(username, agentName, incomingMessage.getContent());
                        msg.setSenderRole("USER");
                        agentSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
                        
                        // 保存用户发给客服的消息
                        saveChatMessage(username, agentName, incomingMessage.getContent(), false, incomingMessage.getSessionId(), "USER");
                        
                    } catch (Exception e) {
                        log.error("[WS-ERR] 发送给客服失败", e);
                    }
                }
            }
            return;
        }

        WebSocketSession targetSession = presenceService.getSession(to);
        if (targetSession != null && targetSession.isOpen()) {
            log.info("[WS-FORWARD] 转发消息给: {}", to);
            OutgoingMessage outgoingMessage = new OutgoingMessage(username, to, incomingMessage.getContent());
            String senderRole = "USER".equalsIgnoreCase(role) ? "USER" : "AGENT";
            outgoingMessage.setSenderRole(senderRole);
            targetSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(outgoingMessage)));
            // 保存普通聊天消息
            saveChatMessage(username, to, incomingMessage.getContent(), false, incomingMessage.getSessionId(), senderRole);
        } else {
            log.warn("[WS-WARN] 目标用户 {} 不在线或 Session 已关闭", to);
        }
    }

    // 提取出来的保存逻辑
    private void saveChatMessage(String from, String to, String content, boolean isAi, Long sessionId, String senderRole) {
        try {
            ChatMessage dbMsg = new ChatMessage();
            dbMsg.setSessionId(sessionId);
            dbMsg.setFromUser(from);
            dbMsg.setToUser(to);
            dbMsg.setContent(content);
            dbMsg.setAi(isAi);
            dbMsg.setSenderRole(senderRole);
            chatMessageRepository.save(dbMsg);
        } catch (Exception e) {
            log.error("保存聊天记录到数据库失败", e);
        }
    }

    private void handleHumanTransfer(String username) {
        log.info("用户 {} 请求转人工", username);
        Set<String> onlineAgents = presenceService.listOnlineAgents();
        if (onlineAgents.isEmpty()) {
            WebSocketSession userSession = presenceService.getSession(username);
            if (userSession != null) {
                sendSystemMessage(userSession, "当前没有客服在线，请稍后再试。");
            }
            return;
        }
        
        // 简单策略：分配给第一个在线客服
        String assignedAgent = onlineAgents.iterator().next();
        USER_AGENT_MAP.put(username, assignedAgent);
        log.info("用户 {} 已分配给客服 {}", username, assignedAgent);

        WebSocketSession agentSession = presenceService.getSession(assignedAgent);
        if (agentSession != null && agentSession.isOpen()) {
            try {
                // 1. 发送转接通知
                OutgoingMessage notifyMsg = new OutgoingMessage("SYSTEM", assignedAgent, "REQUEST_HUMAN:" + username);
                notifyMsg.setSenderRole("SYSTEM");
                agentSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(notifyMsg)));

                // 2. 从数据库加载历史聊天记录并发送给客服
                log.info("[HISTORY] 开始为用户 {} 加载历史记录", username);
                List<ChatMessage> history = chatMessageRepository.findAll().stream()
                        // 过滤掉系统指令，只保留真正的聊天对话
                        .filter(m -> !m.getContent().equals("REQUEST_HUMAN") && !m.getContent().equals("SWITCH_TO_AI"))
                        .filter(m -> m.getFromUser().equals(username) || m.getToUser().equals(username) || m.getFromUser().equals("AI Assistant"))
                        .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                        .toList();
                
                log.info("[HISTORY] 共找到 {} 条有效聊天记录", history.size());
                for (ChatMessage histMsg : history) {
                    // 关键修复：同步给客服时，from 统一设为访客名，这样前端才会把它们渲染在同一个会话里
                    String displayFrom = histMsg.isAi() ? histMsg.getFromUser() : histMsg.getFromUser();
                    
                    OutgoingMessage syncMsg = new OutgoingMessage(displayFrom, assignedAgent, histMsg.getContent());
                    syncMsg.setAi(histMsg.isAi()); // 保留 isAi 标志位用于前端渲染样式
                    syncMsg.setSenderRole(histMsg.getSenderRole()); // 设置发送者角色
                    
                    String json = objectMapper.writeValueAsString(syncMsg);
                    log.info("[HISTORY] 同步消息: {}", json);
                    agentSession.sendMessage(new TextMessage(json));
                }

            } catch (Exception e) {
                log.error("通知客服失败", e);
            }
        }
        
        // 给用户反馈
        WebSocketSession userSession = presenceService.getSession(username);
        if (userSession != null) {
            sendSystemMessage(userSession, "AGENT_JOINED");
        }
    }

    private void sendChatMessage(WebSocketSession session, OutgoingMessage msg) throws IOException {
        String json = objectMapper.writeValueAsString(msg);
        session.sendMessage(new TextMessage(json));
    }

    private void sendSystemMessage(WebSocketSession session, String content) {
        OutgoingMessage outgoingMessage = new OutgoingMessage("SYSTEM", (String) session.getAttributes().get("username"), content);
        outgoingMessage.setSenderRole("SYSTEM");
        try {
            String json = objectMapper.writeValueAsString(outgoingMessage);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}




