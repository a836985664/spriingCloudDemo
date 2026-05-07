package com.example.user.repository;

import com.example.user.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 查找某个用户和客服之间的所有历史记录
    List<ChatMessage> findByFromUserOrToUserOrderByCreatedAtAsc(String from, String to);
}
