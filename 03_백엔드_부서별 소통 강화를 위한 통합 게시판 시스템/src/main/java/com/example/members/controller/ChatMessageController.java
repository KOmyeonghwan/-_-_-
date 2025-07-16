package com.example.members.controller;

import com.example.members.entity.ChatMessage;
import com.example.members.entity.Users;
import com.example.members.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    // 채팅 메시지 전송 (저장)
    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage chatMessage) {
        try {
            // 채팅 메시지 저장
            ChatMessage savedMessage = chatMessageService.saveMessage(chatMessage);

            // 클라이언트에 저장된 메시지의 내용을 반환
            return new ResponseEntity<>(savedMessage, HttpStatus.CREATED); // 성공적으로 저장되면 201 상태 코드
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // 에러 발생 시 500 상태 코드
        }
    }

    // 특정 사용자 간의 채팅 내역 조회
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getChatMessages(@RequestParam("senderId") Integer senderId,
            @RequestParam("receiverId") Integer receiverId) {
        try {
            List<ChatMessage> chatMessages = chatMessageService.getChatMessages(senderId, receiverId);
            if (chatMessages.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 채팅 내역이 없으면 204 상태 코드
            }
            return new ResponseEntity<>(chatMessages, HttpStatus.OK); // 채팅 내역을 200 상태 코드로 반환
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // 에러 발생 시 500 상태 코드
        }
    }

    // 특정 사용자 조회 (보낸 사람이나 받은 사람)
    @GetMapping("/user/{userId}")
    public ResponseEntity<Users> getUser(@PathVariable Integer userId) {
        try {
            Users user = chatMessageService.getUserById(userId);
            return new ResponseEntity<>(user, HttpStatus.OK); // 사용자 정보 반환
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 사용자가 없으면 404 상태 코드
        }
    }
}
