package com.example.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "board_manager")
public class BoardManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String boardCode;

    @Column(nullable = false)
    private String boardName;

    @Column(nullable = true)
    private String deptCode;
}
