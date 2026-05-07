package com.example.user.entity;

import lombok.Data;

@Data
public class IncomingMessage {
    String to;
    String content;
    Long sessionId;  // 会话ID
}
