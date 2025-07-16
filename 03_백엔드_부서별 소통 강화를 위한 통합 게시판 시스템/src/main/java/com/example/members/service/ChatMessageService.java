package com.example.members.service;

import com.example.members.entity.ChatMessage;
import com.example.members.repository.ChatMessageRepository;
import com.example.members.repository.UsersRepository;
import com.example.members.entity.Users; // Users 엔터티 임포트

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UsersRepository usersRepository; // UsersRepository 주입

    // 채팅 메시지 저장
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    // 특정 사용자와의 채팅 내역 조회
    public List<ChatMessage> getChatMessages(Integer senderId, Integer receiverId) {
        return chatMessageRepository.findBySender_UseridxAndReceiver_UseridxOrderByMessageIdAsc(senderId, receiverId);
    }

    // 특정 ID로 사용자 조회 (보낸 사람이나 받은 사람)
    public Users getUserById(Integer userId) {
        return usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}
