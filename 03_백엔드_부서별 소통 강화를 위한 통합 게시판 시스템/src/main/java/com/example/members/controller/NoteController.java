package com.example.members.controller;

import com.example.members.entity.Note;
import com.example.members.entity.Users;
import com.example.members.service.NoteService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // 쪽지 전송
  @PostMapping("/send")
public ResponseEntity<Note> sendNote(@RequestBody Note requestNote, HttpSession session) {
    Users loginUser = (Users) session.getAttribute("user");
    if (loginUser == null || !loginUser.getUserid().equals(requestNote.getSenderId())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Note savedNote = noteService.sendNote(
        requestNote.getSenderId(),
        requestNote.getReceiverId(),
        requestNote.getMessage()
    );
    return ResponseEntity.ok(savedNote);
}



    // 쪽지 삭제
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
        noteService.deleteNote(noteId);
        return ResponseEntity.noContent().build();
    }

    // 받은 쪽지 목록
@GetMapping("/received/{receiverId}")
public ResponseEntity<List<Note>> getReceivedNotes(@PathVariable String receiverId, HttpSession session) {
    Users loginUser = (Users) session.getAttribute("user");
    if (loginUser == null || !loginUser.getUserid().equals(receiverId)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 반환
    }
    List<Note> notes = noteService.getReceivedNotes(receiverId);
    return ResponseEntity.ok(notes);
}

    // 보낸 쪽지 목록
    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<Note>> getSentNotes(@PathVariable String senderId) {
        return ResponseEntity.ok(noteService.getSentNotes(senderId));
    }


    @GetMapping("/notes/received")
public String getReceivedNotes(Model model, Principal principal) {
    String userId = principal.getName();  // 로그인 사용자 ID 받아오기
    List<Note> receivedNotes = noteService.getReceivedNotes(userId);
    model.addAttribute("notes", receivedNotes);
    return "notes/received";  // Mustache 템플릿 경로
}


}
