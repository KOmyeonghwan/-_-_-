package com.example.members.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.members.entity.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    // 특정 사용자의 채팅 내역을 가져오는 쿼리 메서드 예시
    List<ChatMessage> findBySender_UseridxAndReceiver_UseridxOrderByMessageIdAsc(Integer senderId, Integer receiverId);

}
