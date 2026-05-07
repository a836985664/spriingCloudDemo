package com.example.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_history")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;  // 会话ID
    private String fromUser; // 发送者
    private String toUser;   // 接收者
    private String content;  // 消息内容
    private boolean isAi;    // 是否为 AI 回复
    private String senderRole; // 发送者角色：USER（用户）、AGENT（客服）、AI（AI助手）
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
