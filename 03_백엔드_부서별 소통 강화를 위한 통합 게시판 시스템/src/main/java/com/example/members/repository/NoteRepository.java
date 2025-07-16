package com.example.members.repository;

import com.example.members.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByReceiverId(String receiverId);  // 받은 쪽지 목록
    List<Note> findBySenderId(String senderId);      // 보낸 쪽지 목록
}