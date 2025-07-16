package com.example;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.board.entity.BoardManager;
import com.example.board.repository.BoardManagerRepository;
import com.example.members.entity.Users;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalModelAttribute {

    private final BoardManagerRepository boardManagerRepository;

    public GlobalModelAttribute(BoardManagerRepository boardManagerRepository) {
        this.boardManagerRepository = boardManagerRepository;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        List<BoardManager> allBoards = boardManagerRepository.findAll();

        List<BoardManager> fixedBoards = allBoards.stream()
                .filter(board -> board.getDeptCode() == null)
                .collect(Collectors.toList());

        model.addAttribute("fixedBoards", fixedBoards);

        Users user = (Users) session.getAttribute("user");

        if (user != null) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userid", user.getUserid());
            model.addAttribute("user_name", user.getUserName());
            model.addAttribute("loginUser", user);
            boolean isAdmin = user.getAdmin() == 1;
            model.addAttribute("isAdmin", isAdmin);

            List<BoardManager> deptBoards;

            if (isAdmin) {
                // 관리자: 모든 부서 게시판
                deptBoards = allBoards.stream()
                        .filter(board -> board.getDeptCode() != null)
                        .collect(Collectors.toList());
            } else {
                // 일반 사용자: 자신의 부서 코드만
                deptBoards = allBoards.stream()
                        .filter(board -> board.getDeptCode() != null &&
                                board.getDeptCode().toString().equals(user.getDepartment().getDeptCode()))
                        .collect(Collectors.toList());
            }

            model.addAttribute("departmentBoards", deptBoards);
        } else {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("departmentBoards", List.of()); // 비회원: 빈 리스트
        }

        List<Map<String, Object>> menuItems = List.of(
                Map.of("title", "대시보드", "url", "/dashboard"),
                Map.of("title", "회원 관리", "url", "/admin"),
                Map.of("title", "부서 관리", "url", "/admin/department/list"),
                Map.of("title", "게시판 관리", "url", "/admin/board"),
                Map.of("title", "댓글 관리", "url", "/comment/admin"));
                

        model.addAttribute("menuItems", menuItems);
    }
}
