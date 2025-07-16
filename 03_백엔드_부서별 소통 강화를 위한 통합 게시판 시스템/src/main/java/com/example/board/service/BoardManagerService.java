package com.example.board.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.board.entity.BoardManager;
import com.example.board.repository.BoardManagerRepository;

@Service
public class BoardManagerService {

    private final JdbcTemplate jdbcTemplate;
    private final BoardManagerRepository boardManagerRepository;

    public BoardManagerService(JdbcTemplate jdbcTemplate, BoardManagerRepository boardManagerRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.boardManagerRepository = boardManagerRepository;

    }

    private String getTableName(String boardCode) {
        return "board_" + boardCode;
    }

    // 댓글 테이블 이름 생성 공통 메소드
    private String getCommentTableName(String boardCode) {
        return "comment_" + boardCode;
    }

    @Transactional
    public void createBoard(String boardCode, String boardName, String deptCode) {
        // 1. board_manager insert
        BoardManager boardManager = new BoardManager();
        boardManager.setBoardCode(boardCode);
        boardManager.setBoardName(boardName);
        boardManager.setDeptCode(deptCode);
        boardManagerRepository.save(boardManager);

        // 2. 게시판 테이블 생성
        String tableName = "board_" + boardCode;

        String sql = String.format(
                "CREATE TABLE IF NOT EXISTS %s (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "title VARCHAR(255), " +
                        "content TEXT, " +
                        "user_name VARCHAR(30)," +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "user_id VARCHAR(50)," +
                        "views INT DEFAULT 0," +
                        "FOREIGN KEY (user_id) REFERENCES users(userid) ON DELETE CASCADE" +
                        ")",
                tableName);

        jdbcTemplate.execute(sql);

        // ✅ 댓글 테이블도 함께 생성
        String commentTable = getCommentTableName(boardCode);
        String commentSql = String.format(
                "CREATE TABLE IF NOT EXISTS %s (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "post_id BIGINT, " +
                        "user_id VARCHAR(50), " +
                        "user_name VARCHAR(30), " +
                        "content TEXT, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (post_id) REFERENCES %s(id) ON DELETE CASCADE" + // ✅ 연동
                        ")",
                commentTable, tableName);
        jdbcTemplate.execute(commentSql); // ✅ 댓글 테이블 생성
    }

    public Long savePost(String boardCode, String title, String content, String userName, String userId) {
        // ✅ 댓글 테이블 생성 코드는 제거됨!

        // ✅ 게시글 저장
        String tableName = "board_" + boardCode;
        String sql = String.format("INSERT INTO %s (title, content, user_name, user_id) VALUES (?, ?, ?, ?)",
                tableName);
        jdbcTemplate.update(sql, title, content, userName, userId);

        // ✅ 생성된 post ID 반환
        String idQuery = "SELECT LAST_INSERT_ID()";
        return jdbcTemplate.queryForObject(idQuery, Long.class);
    }

    @Transactional
    public void deleteBoard(String boardCode) {
        String commentTableName = "comment_" + boardCode;
        String dropCommentSql = String.format("DROP TABLE IF EXISTS %s", commentTableName);
        jdbcTemplate.execute(dropCommentSql); // ✅ 댓글 테이블도 같이 삭제

        // 1. 게시판 테이블 삭제
        String tableName = "board_" + boardCode;
        String dropSql = String.format("DROP TABLE IF EXISTS %s", tableName);
        jdbcTemplate.execute(dropSql);

        // 2. board_manager에서 삭제
        boardManagerRepository.findByBoardCode(boardCode).ifPresent(boardManagerRepository::delete);
    }

    public boolean isAuthor(Long postId, String userId, String boardCode) {
        String tableName = "board_" + boardCode; // 동적으로 테이블 이름 설정
        String sql = "SELECT user_id FROM " + tableName + " WHERE id = ?";
        try {
            String postUserId = jdbcTemplate.queryForObject(sql, String.class, postId);
            return postUserId != null && postUserId.equals(userId);
        } catch (EmptyResultDataAccessException e) {
            return false; // 게시글이 존재하지 않으면 false
        }
    }

    @Transactional
    public void deletePost(String boardCode, Long postId) {
        String tableName = "board_" + boardCode;
        String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);
        jdbcTemplate.update(sql, postId);
    }

    @Transactional
    public void updatePost(String boardCode, Long postId, String title, String content) {
        String tableName = "board_" + boardCode;
        String sql = String.format("UPDATE %s SET title = ?, content = ? WHERE id = ?", tableName);
        jdbcTemplate.update(sql, title, content, postId);
    }

    public Map<String, Object> getPostDetail(String boardCode, Long postId) {
        String tableName = "board_" + boardCode;

        // ✅ 조회수 증가 코드 삭제됨
        String sql = String.format("SELECT * FROM %s WHERE id = ?", tableName);
        Map<String, Object> postDetail = jdbcTemplate.queryForMap(sql, postId);

        Map<String, Object> previousPost = getAdjacentPost(boardCode, postId, true);
        Map<String, Object> nextPost = getAdjacentPost(boardCode, postId, false);

        postDetail.put("previousPost", previousPost);
        postDetail.put("nextPost", nextPost);

        return postDetail;
    }

    public Page<Map<String, Object>> getPosts(String boardCode, int page, int size) {
        String tableName = "board_" + boardCode;
        int offset = page * size;

        String countSql = String.format("SELECT COUNT(*) FROM %s", tableName);
        int total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = String.format("SELECT * FROM %s ORDER BY id DESC LIMIT ? OFFSET ?", tableName);
        List<Map<String, Object>> content = jdbcTemplate.queryForList(sql, size, offset);

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }

    public Page<Map<String, Object>> searchPosts(String boardCode, String searchType, String keyword, int page,
            int size) {
        String tableName = "board_" + boardCode;
        int offset = page * size;

        // 검색할 컬럼 정하기 (SQL 인젝션 위험 없도록 사전에 값 제한하는 게 좋음)
        String column;
        if ("user_name".equals(searchType)) {
            column = "user_name";
        } else {
            column = "title"; // 기본값
        }

        String countSql = String.format("SELECT COUNT(*) FROM %s WHERE %s LIKE ?", tableName, column);
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, "%" + keyword + "%");

        String sql = String.format("SELECT * FROM %s WHERE %s LIKE ? ORDER BY id DESC LIMIT ? OFFSET ?", tableName,
                column);
        List<Map<String, Object>> content = jdbcTemplate.queryForList(sql, "%" + keyword + "%", size, offset);

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }

    public Map<String, Object> getAdjacentPost(String boardCode, Long postId, boolean isPrevious) {
        String tableName = "board_" + boardCode;

        // 이전 게시글 조회 (현재 게시글보다 ID가 작은 게시글 중 최신 것)
        String sql = isPrevious ? String.format("SELECT * FROM %s WHERE id < ? ORDER BY id DESC LIMIT 1", tableName)
                : String.format("SELECT * FROM %s WHERE id > ? ORDER BY id ASC LIMIT 1", tableName);

        try {
            return jdbcTemplate.queryForMap(sql, postId);
        } catch (EmptyResultDataAccessException e) {
            return null; // 없으면 null 반환
        }
    }

    @Transactional
    public void incrementViews(Long postId, String boardCode) {
        String tableName = "board_" + boardCode;
        String sql = String.format("UPDATE %s SET views = views + 1 WHERE id = ?", tableName);
        jdbcTemplate.update(sql, postId);
    }

    // 대시보드 관련 게시글 수
    public long countTotalBoards() {
        return boardManagerRepository.count(); // board_manager 테이블의 총 게시판 수
    }

    public long countTotalPosts() {
        long totalPosts = 0;

        // 모든 게시판을 가져옴 (board_manager 테이블에서)
        List<BoardManager> boards = boardManagerRepository.findAll();

        // 각 게시판에 대해 게시글 수를 계산
        for (BoardManager board : boards) {
            String boardCode = board.getBoardCode();
            String tableName = "board_" + boardCode; // 동적으로 테이블 이름 설정

            // 게시판에 해당하는 테이블에서 게시글 수를 계산하는 쿼리
            String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
            totalPosts += jdbcTemplate.queryForObject(sql, Long.class);
        }

        return totalPosts;
    }

    public List<Map<String, Object>> getLatestPostsForAllBoards() {
        List<Map<String, Object>> latestPostsList = new ArrayList<>();
        List<BoardManager> boards = boardManagerRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (BoardManager board : boards) {
            List<Map<String, Object>> posts = getLatestPosts(board.getBoardCode());

            for (Map<String, Object> post : posts) {
                Map<String, Object> postDetails = new HashMap<>();
                postDetails.put("boardName", board.getBoardName());
                postDetails.put("boardCode", board.getBoardCode());
                postDetails.put("id", post.get("id"));
                postDetails.put("title", post.getOrDefault("title", "제목 없음"));
                postDetails.put("content", post.getOrDefault("content", ""));
                postDetails.put("userName", post.getOrDefault("user_name", "알 수 없음"));

                Object createdAtObj = post.get("created_at");
                System.out.println(
                        "created_at type: " + (createdAtObj != null ? createdAtObj.getClass().getName() : "null"));
                System.out.println("created_at value: " + createdAtObj);

                if (createdAtObj instanceof LocalDateTime) {
                    LocalDateTime ldt = (LocalDateTime) createdAtObj;
                    String formattedDate = ldt.format(formatter);
                    postDetails.put("createdAt", formattedDate);
                } else {
                    postDetails.put("createdAt", "");
                }

                latestPostsList.add(postDetails);
            }
        }
        return latestPostsList;
    }

    public List<Map<String, Object>> getLatestPosts(String boardCode) {
        String tableName = "board_" + boardCode;

        String sql = String.format(
                "SELECT id, title, content, user_name, created_at " +
                        "FROM %s ORDER BY created_at DESC LIMIT 5",
                tableName);

        return jdbcTemplate.queryForList(sql);
    }

    // 오늘 등록된 게시글
    public long countTodayPosts() {
        long totalTodayPosts = 0;

        // 오늘 날짜
        LocalDate today = LocalDate.now();
        String start = today.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // 오늘 00:00:00
        String end = today.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // 내일
                                                                                                                  // 00:00:00

        // 모든 게시판 조회
        List<BoardManager> boards = boardManagerRepository.findAll();

        for (BoardManager board : boards) {
            String boardCode = board.getBoardCode();
            String tableName = "board_" + boardCode;

            // 오늘 작성된 게시글 수 조회
            String sql = String.format("SELECT COUNT(*) FROM %s WHERE created_at >= '%s' AND created_at < '%s'",
                    tableName, start, end);

            long count = jdbcTemplate.queryForObject(sql, Long.class);
            totalTodayPosts += count;
        }

        return totalTodayPosts;
    }

    // 공지사항 최신글 5개 가져오기
    public List<Map<String, Object>> LatestPosts(String boardCode, int limit) {
        String tableName = getTableName(boardCode);
        String sql = String.format(
                "SELECT id, title, DATE_FORMAT(created_at, '%%Y-%%m-%%d') AS date FROM %s ORDER BY id DESC LIMIT ?",
                tableName);
        return jdbcTemplate.queryForList(sql, limit);
    }

}
