package com.example.members.service;

import com.example.members.entity.Note;
import com.example.members.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    public Note sendNote(String senderId, String receiverId, String message) {
        Note note = new Note();
        note.setSenderId(senderId);
        note.setReceiverId(receiverId);
        note.setMessage(message);
        return noteRepository.save(note);
    }

    public void deleteNote(Long noteId) {
        noteRepository.deleteById(noteId);
    }

    public List<Note> getReceivedNotes(String receiverId) {
        return noteRepository.findByReceiverId(receiverId);
    }

    public List<Note> getSentNotes(String senderId) {
        return noteRepository.findBySenderId(senderId);
    }

    public Optional<Note> getNoteById(Long noteId) {
        return noteRepository.findById(noteId);
    }
}