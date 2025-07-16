package com.example.members.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Long noteId;

    @Column(name = "sender_id", length = 50, nullable = false)
    private String senderId;  // users.userid

    @Column(name = "receiver_id", length = 50, nullable = false)
    private String receiverId;  // users.userid

    
    @Column(nullable = false)
    private String message;
}
