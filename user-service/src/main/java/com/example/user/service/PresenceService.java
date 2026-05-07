package com.example.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PresenceService {
    private  Map<String, WebSocketSession> sessions = new ConcurrentHashMap();
    private final Set<String> onlineAgents = ConcurrentHashMap.newKeySet();

    public void addSession(String username, String role, WebSocketSession session, Map<String, WebSocketSession> sessions) {
        log.info("添加客服端：{},{}",username,role);
         sessions.put(username, session);
        if ("AGENT".equals(role)) {
            onlineAgents.add(username);
        }
        this.sessions=sessions;
    }

    public void removeSession(String username,String role){
        if ("AGENT".equals(role)) {
            onlineAgents.remove(username);
        }
    }
    public WebSocketSession getSession(String username){
        return sessions.get(username);
    }
    public Set<String> listOnlineAgents(){
        return onlineAgents.stream().collect(Collectors.toUnmodifiableSet());
    }
    public boolean isOnlineAgent(String username){
        return onlineAgents.contains(username);
    }
}
