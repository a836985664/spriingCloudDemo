package com.example.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final ChatClient.Builder chatClientBuilder;

    public String chatWithAssistant(String userMessage, Long userId) {
        log.info("用户 {} 正在与 AI 助理对话: {}", userId, userMessage);
        
        // 关键词检测：转人工
        if (userMessage.contains("转人工") || userMessage.contains("人工客服")) {
            return "__TRANSFER_TO_HUMAN__";
        }

        ChatClient chatClient = chatClientBuilder.build();
        String prompt = String.format(
            "你是一个贴心的个人助理。用户 ID 是 %d。请友好、简洁地回答以下问题：\n%s",
            userId, userMessage
        );

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI 助理服务调用失败", e);
            return "抱歉，我现在脑子有点乱，请稍后再试。";
        }
    }
}
