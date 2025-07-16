package com.example.board.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.board.entity.BoardManager;

import java.util.List;
import java.util.Optional;

public interface BoardManagerRepository extends JpaRepository<BoardManager, Long> {
    Optional<BoardManager> findByBoardCode(String boardCode);
    Optional<BoardManager> findByBoardName(String boardName);
    List<BoardManager> findByDeptCode(String deptCode);
}