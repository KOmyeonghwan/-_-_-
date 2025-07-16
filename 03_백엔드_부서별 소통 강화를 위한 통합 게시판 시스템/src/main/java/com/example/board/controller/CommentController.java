package com.example.board.controller;

import com.example.board.service.CommentService;
import com.example.members.entity.Users;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;

    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addComment(@RequestBody Map<String, Object> requestData,
            HttpSession httpSession) {
        String boardCode = (String) requestData.get("boardCode");
        Long postId = Long.parseLong(requestData.get("postId").toString());
        String content = (String) requestData.get("content");

        Users loginUser = (Users) httpSession.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        String userId = loginUser.getUserid();
        String userName = loginUser.getUserName();

        commentService.addComment(boardCode, postId, userId, userName, content);
        response.put("success", true);
        return response;
    }

    @GetMapping("/list")
    @ResponseBody
    public List<Map<String, Object>> getComments(@RequestParam String boardCode,
            @RequestParam Long postId,
            HttpSession session) {
        Users loginUser = (Users) session.getAttribute("user");
        String currentUserId = loginUser != null ? loginUser.getUserid() : null;

        List<Map<String, Object>> comments = commentService.getComments(boardCode, postId);

        for (Map<String, Object> comment : comments) {
            String commentUserId = (String) comment.get("user_id");
            comment.put("isAuthor", currentUserId != null && currentUserId.equals(commentUserId));
        }

        return comments;
    }

    @PostMapping("/delete/{commentId}")
    @ResponseBody
    public Map<String, Object> deleteComment(@PathVariable Long commentId,
            @RequestParam String boardCode,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        Users loginUser = (Users) session.getAttribute("user");

        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        String userId = loginUser.getUserid();
        boolean isAdmin = loginUser.getAdmin() == 1; // admin 필드가 1이면 관리자

        // 관리자 아닐 경우에만 작성자인지 확인
        if (!isAdmin) {
            boolean isAuthor = commentService.isCommentAuthor(boardCode, commentId, userId);
            if (!isAuthor) {
                response.put("success", false);
                response.put("message", "본인만 삭제할 수 있습니다.");
                return response;
            }
        }

        // 관리자이거나 작성자이면 삭제 가능
        commentService.deleteComment(boardCode, commentId);
        response.put("success", true);
        return response;
    }

    // 관리자용 전체 댓글 조회
    @GetMapping("/admin/list")
    @ResponseBody
    public Map<String, Object> getCommentsAdmin(
            @RequestParam String boardCode,
            @RequestParam(defaultValue = "1") int page) { // page 기본값 1
        int size = 10; // 한 페이지당 10개 게시글
        return commentService.getGroupedCommentsWithIndex(boardCode, page, size);
    }

    @GetMapping("/admin")
    public String commentAdminPage(
            @RequestParam(value = "boardCode", required = false) String boardCode,
            @RequestParam(value = "boardName", required = false) String boardName,
            Model model, HttpSession session) {

        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getAdmin() != 1) {
            return "redirect:/login?redirect=/dashboard"; // 관리자 아니면 로그인으로 이동
        }

        // boardCode, boardName이 없더라도 동작하도록
        model.addAttribute("boardCode", boardCode);
        model.addAttribute("boardName", boardName);

        // 게시판 목록을 모델에 넣기 (탭 메뉴용)
        List<Map<String, String>> boards = commentService.getBoardList();
        model.addAttribute("boards", boards);

        return "adminboard/comment";
    }
}
