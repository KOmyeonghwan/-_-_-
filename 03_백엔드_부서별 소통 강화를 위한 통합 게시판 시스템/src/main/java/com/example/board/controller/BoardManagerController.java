package com.example.board.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.board.entity.BoardManager;
import com.example.board.repository.BoardManagerRepository;
import com.example.board.service.BoardManagerService;
import com.example.members.entity.Users;

import jakarta.servlet.http.HttpSession;

import java.text.Collator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
public class BoardManagerController {

    private final BoardManagerService boardService;
    private final BoardManagerRepository boardRepo;
    private final JdbcTemplate jdbcTemplate;

    /*****************************************
     * 신경쓰지 말기
     *****************************************/
    public BoardManagerController(BoardManagerService boardService, BoardManagerRepository boardRepo,
            JdbcTemplate jdbcTemplate) {
        this.boardService = boardService;
        this.boardRepo = boardRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    /***************************************************************************************/

    @GetMapping("/admin/board")
    public String home(Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getAdmin() != 1) {
            return "redirect:/login?redirect=/dashboard"; // 관리자 아니면 로그인으로 이동
        }

        List<BoardManager> boards = boardRepo.findAll();
        boards.sort(Comparator.comparing(BoardManager::getBoardName, Collator.getInstance(Locale.KOREAN)));
        model.addAttribute("boards", boards);
        return "adminboard/adminboard";
    }

    @GetMapping("/board/write")
    public String viewBoard(@RequestParam("boardCode") String boardCode, Model model) {
        String boardName = "기본 게시판 이름"; // 기본값 설정

        try {
            // board_manager 테이블에서 board_name을 가져오는 쿼리
            String query = "SELECT board_name FROM board_manager WHERE board_code = ?";
            boardName = jdbcTemplate.queryForObject(query, String.class, boardCode);
        } catch (Exception e) {
        }

        model.addAttribute("boardName", boardName);
        model.addAttribute("boardCode", boardCode);

        return "adminboard/board";
    }

    @PostMapping("/board/{boardCode}/post")
    public String postToBoard(@PathVariable("boardCode") String boardCode,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("user_id") String user_id,
            @ModelAttribute("user_name") String userName,
            @RequestParam(value = "search", required = false) String search) {
        boardService.savePost(boardCode, title, content, userName, user_id);
        if (search != null && !search.isEmpty()) {
            return "redirect:/admin/boardlist?boardCode=" + boardCode + "&search=" + search;
        }

        return "redirect:/admin/boardlist?boardCode=" + boardCode;
    }

    @PostMapping("/admin/board/create")
    @ResponseBody
    public Map<String, Object> createBoardTable(@RequestParam("boardCode") String boardCode,
            @RequestParam("boardName") String boardName,
            @RequestParam(value = "deptCode", required = false) String deptCode,
            Model model) {

        Map<String, Object> result = new HashMap<>();

        // 중복 검사
        if (boardRepo.findByBoardCode(boardCode).isPresent() || boardRepo.findByBoardName(boardName).isPresent()) {
            result.put("status", "error");
            result.put("message", "이미 존재하는 게시판 코드나 이름입니다.");
            return result;
        }

        // 저장
        boardService.createBoard(boardCode, boardName, deptCode);

        result.put("status", "success");
        result.put("message", "성공적으로 저장되었습니다.");
        return result;
    }

    @PostMapping("/admin/board/{boardCode}/delete")
    public String deleteBoard(@PathVariable("boardCode") String boardCode) {
        boardService.deleteBoard(boardCode);
        return "redirect:/admin/board";
    }

    // ==============================================================================================

    @GetMapping("/admin/boardlist")
    public String listBoards(
            @RequestParam(name = "boardCode", required = false) String boardCode,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "searchType", defaultValue = "title") String searchType,
            @RequestParam(name = "page", defaultValue = "1") int page,
            HttpSession session,
            Model model) {

        final int PAGE_SIZE = 10;

        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getAdmin() != 1) {
            return "redirect:/login?redirect=/dashboard"; // 관리자 아니면 로그인으로 이동
        }

        // 1. 전체 게시판 목록 조회 (탭 메뉴용)
        List<BoardManager> boards = boardRepo.findAll();
        // 게시판 이름(boardName)을 기준으로 가나다순 정렬
        boards.sort(Comparator.comparing(BoardManager::getBoardName, Collator.getInstance(Locale.KOREAN)));
        model.addAttribute("boards", boards);

        // 2. 기본 게시판 선택 (boardCode가 없으면 첫 번째 게시판)
        if ((boardCode == null || boardCode.isEmpty()) && !boards.isEmpty()) {
            boardCode = boards.get(0).getBoardCode(); // 첫 번째 게시판을 기본으로 설정
        }

        // boardCode를 final로 선언하여 화살표 함수에서 사용할 수 있도록 함
        final String finalBoardCode = boardCode;

        // boardName 찾기 (탭메뉴 사용)
        Optional<BoardManager> selectedBoard = boards.stream()
                .filter(b -> b.getBoardCode().equals(finalBoardCode)) // finalBoardCode 사용
                .findFirst();

        // DB의 boardName과 id(번호) 모델에 추가
        selectedBoard.ifPresent(board -> {
            model.addAttribute("boardName", board.getBoardName());
            model.addAttribute("id", board.getId());
        });

        // 검색어 기본값 설정
        if (search == null) {
            search = "";
        }

        // 3. 게시글 목록 조회
        if (boardCode != null && !boardCode.isEmpty()) {
            String tableName = "board_" + boardCode;

            // 전체 게시글 수 조회
            String countSql = "SELECT COUNT(*) FROM " + tableName;
            int totalCount;
            if (!search.isEmpty()) {
                countSql += " WHERE " + searchType + " LIKE ?";
                totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, "%" + search + "%");
            } else {
                totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            }

            // 페이징 계산
            int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
            int offset = (page - 1) * PAGE_SIZE;

            // 게시글 목록 쿼리
            String selectSql = "SELECT * FROM " + tableName;
            List<Map<String, Object>> posts;

            if (!search.isEmpty()) {
                selectSql += " WHERE " + searchType + " LIKE ? ORDER BY id DESC LIMIT ? OFFSET ?";
                posts = jdbcTemplate.queryForList(selectSql, "%" + search + "%", PAGE_SIZE, offset);
            } else {
                selectSql += " ORDER BY id DESC LIMIT ? OFFSET ?";
                posts = jdbcTemplate.queryForList(selectSql, PAGE_SIZE, offset);
            }

            // 포맷을 변경한 날짜 추가
            for (Map<String, Object> post : posts) {
                Object createdAtObj = post.get("created_at");

                if (createdAtObj != null) {
                    if (createdAtObj instanceof LocalDateTime) {
                        // LocalDateTime을 String으로 포맷하여 변환
                        LocalDateTime dateTime = (LocalDateTime) createdAtObj;
                        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        post.put("created_at", formattedDate);
                    }
                }
            }

            // 게시글 번호 재정렬
            int displayNumber = totalCount - offset;
            for (Map<String, Object> post : posts) {
                post.put("number", displayNumber--);
            }

            // 모델 데이터 추가
            model.addAttribute("posts", posts);
            model.addAttribute("boardCode", boardCode);
            //검색 관련
            model.addAttribute("search", search);
            model.addAttribute("searchType", searchType);
            model.addAttribute("isTitleSearch", "title".equals(searchType));
            model.addAttribute("isWriterSearch", "user_name".equals(searchType));

            // 페이지 리스트 생성
            List<Map<String, Object>> pageList = new ArrayList<>();
            for (int i = 1; i <= totalPages; i++) {
                Map<String, Object> pageMap = new HashMap<>();
                pageMap.put("num", i);
                pageMap.put("active", i == page); // 현재 페이지 여부 설정
                pageList.add(pageMap);
            }

            // 페이징 관련 데이터 추가
            model.addAttribute("pageList", pageList);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
        }

        return "adminboard/boardlist";
    }

    @GetMapping("/admin/boardview/{postId}")
    public String viewPost(@PathVariable("postId") Long postId, @RequestParam("boardCode") String boardCode,
            HttpSession session, Model model) {

        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getAdmin() != 1) {
            return "redirect:/login?redirect=/dashboard"; // 관리자 아니면 로그인으로 이동
        }

        // board_manager 테이블에서 board_name을 가져오는 SQL
        String sqlBoardName = "SELECT board_name FROM multi.board_manager WHERE board_code = ?";
        String boardName = jdbcTemplate.queryForObject(sqlBoardName, String.class, boardCode);

        // 게시글 내용 가져오기
        String tableName = "board_" + boardCode;
        String sqlPost = String.format("SELECT * FROM %s WHERE id = ?", tableName);
        Map<String, Object> post = jdbcTemplate.queryForMap(sqlPost, postId);

        // 날짜 포맷
        Object createdAtObj = post.get("created_at");

        if (createdAtObj instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) createdAtObj;
            String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            post.put("created_at", formattedDate); // "2025-05-12" 형식으로 변환
        }

        // 모델에 게시글 데이터와 boardCode, boardName 추가
        model.addAttribute("post", post);
        model.addAttribute("boardCode", boardCode);
        model.addAttribute("boardName", boardName);

        return "adminboard/boardview"; // 게시글 상세보기 페이지로 이동
    }

    @GetMapping("/admin/boardedit/{postId}")
    public String editPost(@PathVariable("postId") Long postId, @RequestParam("boardCode") String boardCode,
            HttpSession session, Model model) {

        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getAdmin() != 1) {
            return "redirect:/login?redirect=/dashboard"; // 관리자 아니면 로그인으로 이동
        }

        String tableName = "board_" + boardCode;
        String sql = String.format("SELECT * FROM %s WHERE id = ?", tableName);
        Map<String, Object> post = jdbcTemplate.queryForMap(sql, postId);

        model.addAttribute("title", post.get("title"));
        model.addAttribute("content", post.get("content"));
        model.addAttribute("postId", postId);
        model.addAttribute("boardCode", boardCode);

        return "adminboard/boardedit";
    }

    @PostMapping("/board/edit/{postId}")
    public String updatePost(@PathVariable("postId") Long postId,
            @RequestParam("boardCode") String boardCode,
            @RequestParam("title") String title,
            @RequestParam("content") String content) {
        String tableName = "board_" + boardCode;
        String sql = String.format("UPDATE %s SET title = ?, content = ? WHERE id = ?", tableName);
        jdbcTemplate.update(sql, title, content, postId);

        return "redirect:/admin/boardlist?boardCode=" + boardCode;
    }

    @PostMapping("/board/delete/{postId}")
    public String deletePost(@PathVariable("postId") Long postId, @RequestParam("boardCode") String boardCode) {
        String tableName = "board_" + boardCode;
        String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);
        jdbcTemplate.update(sql, postId);

        return "redirect:/admin/boardlist?boardCode=" + boardCode;
    }
}
