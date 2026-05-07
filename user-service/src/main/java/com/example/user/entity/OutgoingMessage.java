package com.example.user.entity;

import lombok.Data;

import java.time.Instant;

@Data
public class OutgoingMessage {
    String from;
    String to;
    String content;
    Instant ts;
    boolean isAi = false;
    String senderRole; // 发送者角色：USER/AGENT/AI

    public OutgoingMessage(String from, String to, String content) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.ts = Instant.now();
    }
}
