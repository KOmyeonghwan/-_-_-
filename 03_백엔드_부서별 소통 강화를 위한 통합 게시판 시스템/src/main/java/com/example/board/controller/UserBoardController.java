package com.example.board.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.board.entity.BoardManager;

import com.example.board.repository.BoardManagerRepository;
import com.example.board.service.BoardManagerService;
import com.example.members.entity.Users;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserBoardController {
    private final BoardManagerService boardManagerService;
    private final BoardManagerRepository boardManagerRepository;

    @GetMapping("/boards/{boardCode}")
    public String boardList(@PathVariable("boardCode") String boardCode,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", defaultValue = "title") String searchType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model,
            HttpSession session) {

        Page<Map<String, Object>> paging;

        if (keyword != null && !keyword.isEmpty()) {
            paging = boardManagerService.searchPosts(boardCode, searchType, keyword, page, size); // searchType 전달
        } else {
            paging = boardManagerService.getPosts(boardCode, page, size);
        }

        BoardManager boardManager = boardManagerRepository.findByBoardCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판 코드입니다: " + boardCode));

        model.addAttribute("board", paging);
        model.addAttribute("boardCode", boardCode);
        model.addAttribute("boardName", boardManager.getBoardName());

        int totalPages = paging.getTotalPages();
        int currentPage = paging.getNumber(); // 0-based
        int pageSize = paging.getSize();

        int startPage = Math.max(1, currentPage - 2 + 1); // +1 to convert to 1-based
        int endPage = Math.min(totalPages, currentPage + 2 + 1);

        List<Map<String, Object>> pages = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            Map<String, Object> pageInfo = new HashMap<>();
            pageInfo.put("index", i - 1); // 실제 요청에 보낼 page (0-based)
            pageInfo.put("display", i); // 사용자에게 보여줄 page 번호 (1-based)
            pages.add(pageInfo);
        }

        List<Map<String, Object>> boardContent = new ArrayList<>();
        int totalElements = (int) paging.getTotalElements(); // 전체 게시글 수
        int postIndex = totalElements - (currentPage * size); // 현재 페이지의 첫 번째 게시글 번호

        // DateTimeFormatter 추가
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Map<String, Object> post : paging.getContent()) {
            post.put("originId", post.get("id")); // 원래 ID 저장
            post.put("id", postIndex--); // UI 표시용 번호 재정의
            // boardContent.add(post);

            // 날짜 포맷 변경
            if (post.get("created_at") != null) {
                post.put("created_at", post.get("created_at").toString().substring(0, 10)); // "yyyy-MM-dd"
            }
            boardContent.add(post);
        }

        model.addAttribute("boardContent", boardContent);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("searchType", searchType);
        model.addAttribute("isTitleSearch", searchType.equals("title"));
        model.addAttribute("isUserSearch", searchType.equals("user_name"));
        model.addAttribute("pages", pages);
        model.addAttribute("hasPrevious", paging.hasPrevious());
        model.addAttribute("hasNext", paging.hasNext());
        model.addAttribute("prevPage", currentPage - 1);
        model.addAttribute("nextPage", currentPage + 1);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("currentPage", currentPage + 1); // 1-based current page

        Users loginUser = (Users) session.getAttribute("user");
        boolean isAdmin = loginUser != null && loginUser.getAdmin() == 1;
        boolean isNoticeBoard = "notice".equals(boardCode); // 공지사항 코드로 판단

        boolean showWriteButton = !isNoticeBoard || isAdmin; // 공지사항이 아니거나 관리자인 경우만 true

        model.addAttribute("showWriteButton", showWriteButton);
        return "userboard/userboard";
    }

    @GetMapping("/board/edit/{postId}")
    public String editPost(@PathVariable Long postId,
            @RequestParam("boardCode") String boardCode,
            HttpSession session,
            Model model) {
        Users loginUser = (Users) session.getAttribute("user");
        if (loginUser == null) {
            return "redirect:/login";
        }

        if (!boardManagerService.isAuthor(postId, loginUser.getUserid(), boardCode)) {
            return "redirect:/boards/" + boardCode;
        }

        Map<String, Object> post = boardManagerService.getPostDetail(boardCode, postId);
        if (post == null) {
            return "redirect:/boards/" + boardCode;
        }

        model.addAttribute("board", post); // key를 'board'로 줘야 {{board.title}} 등에서 사용 가능
        model.addAttribute("boardCode", boardCode);
        return "userboard/userboardedit"; // 수정 화면
    }

    @PostMapping("/user/board/edit/{postId}")
    public String updatePost(@PathVariable Long postId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("boardCode") String boardCode,
            HttpSession session) {
        Users loginUser = (Users) session.getAttribute("user");
        if (loginUser == null) {
            return "redirect:/login";
        }

        if (!boardManagerService.isAuthor(postId, loginUser.getUserid(), boardCode)) {
            return "redirect:/boards/" + boardCode;
        }

        boardManagerService.updatePost(boardCode, postId, title, content);
        return "redirect:/board/view/" + postId + "?boardCode=" + boardCode;
    }

    @GetMapping("/board/view/{postId}")
    public String viewPost(@PathVariable("postId") Long postId,
            @RequestParam("boardCode") String boardCode,
            HttpSession session,
            Model model) {

        Users loginUser = (Users) session.getAttribute("user");
        Integer userId = null;

        if (loginUser != null) {
            userId = loginUser.getUseridx(); // 고유 식별자
        }

        Map<Integer, Set<Long>> viewedPostsMap = (Map<Integer, Set<Long>>) session.getAttribute("viewedPosts");
        if (viewedPostsMap == null) {
            viewedPostsMap = new HashMap<>();
        }

        Set<Long> userViewedPosts = viewedPostsMap.getOrDefault(userId, new HashSet<>());

        if (!userViewedPosts.contains(postId)) {
            boardManagerService.incrementViews(postId, boardCode);
            userViewedPosts.add(postId);
            viewedPostsMap.put(userId, userViewedPosts);
            session.setAttribute("viewedPosts", viewedPostsMap);
        }

        Map<String, Object> post = boardManagerService.getPostDetail(boardCode, postId);
        if (post == null) {
            return "redirect:/boards/" + boardCode;
        }

        boolean isAuthor = loginUser != null && loginUser.getUserid().equals(post.get("user_id"));

        // 날짜 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (post.get("created_at") != null) {
            post.put("created_at", formatter.format((java.time.LocalDateTime) post.get("created_at")));
        }

        Map<String, Object> previousPost = boardManagerService.getAdjacentPost(boardCode, postId, true);
        Map<String, Object> nextPost = boardManagerService.getAdjacentPost(boardCode, postId, false);

        Page<Map<String, Object>> paging = boardManagerService.getPosts(boardCode, 0, 5);
        List<Map<String, Object>> boardContent = new ArrayList<>();
        for (Map<String, Object> p : paging.getContent()) {
            if (p.get("created_at") != null) {
                p.put("created_at", formatter.format((java.time.LocalDateTime) p.get("created_at")));
            }
            boardContent.add(p);
        }

        model.addAttribute("previousPost", previousPost);
        model.addAttribute("nextPost", nextPost);
        model.addAttribute("boardContent", boardContent);
        model.addAttribute("title", post.get("title"));
        model.addAttribute("content", post.get("content"));
        model.addAttribute("user_name", post.get("user_name"));
        model.addAttribute("created_at", post.get("created_at"));
        model.addAttribute("views", post.get("views"));
        model.addAttribute("id", post.get("id"));
        model.addAttribute("boardCode", boardCode);
        model.addAttribute("isAuthor", isAuthor);

        return "userboard/userview";
    }

    @GetMapping("/write")
    public String writeGet(@RequestParam("boardCode") String boardCode, HttpSession session, Model model) {
        Users loginUser = (Users) session.getAttribute("user");
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("boardCode", boardCode); // 이걸 추가해야 글쓰기 폼에서 boardCode 출력 가능

        return "userboard/userboardwrite";
    }

    @PostMapping("/write")
    public String writePost(@RequestParam("boardCode") String boardCode,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            HttpSession session) {

        Users loginUser = (Users) session.getAttribute("user");
        if (loginUser == null) {
            return "redirect:/login";
        }

        String userName = loginUser.getUserName();
        String userId = loginUser.getUserid();

        // 글을 작성하고 ID를 받아오기
        boardManagerService.savePost(boardCode, title, content, userName, userId);

        // 게시판 목록 페이지로 리디렉션
        return "redirect:/boards/" + boardCode;
    }

    @PostMapping("/delete/{boardCode}/{postId}")
    public String deletePost(@PathVariable("boardCode") String boardCode,
            @PathVariable("postId") Long postId, HttpSession session) {

        Users loginUser = (Users) session.getAttribute("user");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 게시물 작성자 확인
        if (!boardManagerService.isAuthor(postId, loginUser.getUserid(), boardCode)) {
            return "redirect:/boards/" + boardCode; // 권한이 없으면 게시판 목록으로 리다이렉트
        }

        boardManagerService.deletePost(boardCode, postId);
        return "redirect:/boards/" + boardCode;
    }

}
