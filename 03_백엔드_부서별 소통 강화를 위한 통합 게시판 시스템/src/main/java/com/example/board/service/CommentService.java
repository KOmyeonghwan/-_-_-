package com.example.board.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class CommentService {

    private final JdbcTemplate jdbcTemplate;

    public CommentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 댓글 테이블 생성
    public void createCommentTable(String boardCode) {
        // "comment_"와 boardCode를 결합하여 테이블 이름을 생성
        String commentTable = "comment_" + boardCode;

        // 테이블 이름이 "comment_comment_free"와 같이 두 번 붙지 않도록 수정
        // 아래 SQL문에서 comment_ 접두어가 두 번 붙지 않도록 "comment_" + boardCode를 사용
        String createCommentSql = String.format(
                "CREATE TABLE IF NOT EXISTS %s (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "post_id BIGINT, " +
                        "user_id VARCHAR(50), " +
                        "user_name VARCHAR(30), " +
                        "content TEXT, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (post_id) REFERENCES board_%s(id)) ",
                commentTable, boardCode);

        jdbcTemplate.execute(createCommentSql);
    }

    @Transactional
    public void addComment(String boardCode, Long postId, String userId, String userName, String content) {
        String tableName = "comment_" + boardCode; // 게시판 코드에 맞는 댓글 테이블 이름 생성
        String sql = String.format("INSERT INTO %s (post_id, user_id, user_name, content) VALUES (?, ?, ?, ?)",
                tableName);

        try {
            jdbcTemplate.update(sql, postId, userId, userName, content);
        } catch (Exception e) {
            e.printStackTrace(); // 예외가 발생하면 로그를 출력
        }
    }

    // 댓글 조회
    public List<Map<String, Object>> getComments(String boardCode, Long postId) {
        // comment_<boardCode> 형식으로 테이블 이름 생성
        String tableName = "comment_" + boardCode; // "comment_" + boardCode로 정확한 테이블 이름 생성
        String sql = String.format("SELECT * FROM %s WHERE post_id = ? ORDER BY created_at ASC", tableName);
        return jdbcTemplate.queryForList(sql, postId);
    }

    // 댓글 작성자인지 확인
    public boolean isCommentAuthor(String boardCode, Long commentId, String userId) {
        String tableName = "comment_" + boardCode;
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE id = ? AND user_id = ?", tableName);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, commentId, userId);
        return count != null && count > 0;
    }

    // 댓글 삭제
    public void deleteComment(String boardCode, Long commentId) {
        String tableName = "comment_" + boardCode;
        String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);
        jdbcTemplate.update(sql, commentId);
    }

    // 관리자용 - 특정 게시판의 전체 댓글 보기 (게시글 제목 포함)
    public List<Map<String, Object>> getAllComments(String boardCode) {
        String commentTable = "comment_" + boardCode;
        String postTable = "board_" + boardCode;

        String sql = String.format(
                "SELECT c.*, p.title AS post_title " +
                        "FROM %s c " +
                        "JOIN %s p ON c.post_id = p.id " +
                        "ORDER BY c.created_at DESC",
                commentTable, postTable);

        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, String>> getBoardList() {
        String sql = "SELECT board_code AS boardCode, board_name AS boardName FROM board_manager ORDER BY board_name";

        List<Map<String, Object>> rawList = jdbcTemplate.queryForList(sql);

        List<Map<String, String>> boardList = new ArrayList<>();

        for (Map<String, Object> row : rawList) {
            Map<String, String> board = new HashMap<>();
            board.put("boardCode", (String) row.get("boardCode"));
            board.put("boardName", (String) row.get("boardName"));
            boardList.add(board);
        }

        return boardList;
    }

    public Map<String, Object> getGroupedCommentsWithIndex(String boardCode, int page, int size) {
        String commentTable = "comment_" + boardCode;
        String postTable = "board_" + boardCode;

        String sql = String.format(
                "SELECT c.*, p.title AS post_title, p.id AS post_id " +
                        "FROM %s c " +
                        "JOIN %s p ON c.post_id = p.id " +
                        "ORDER BY c.post_id DESC, c.created_at ASC",
                commentTable, postTable);

        List<Map<String, Object>> rawComments = jdbcTemplate.queryForList(sql);

        // post_id 기준으로 그룹핑
        Map<Long, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> comment : rawComments) {
            Long postId = ((Number) comment.get("post_id")).longValue();
            grouped.putIfAbsent(postId, new HashMap<>());
            Map<String, Object> postGroup = grouped.get(postId);
            postGroup.put("postTitle", comment.get("post_title"));
            postGroup.put("postId", postId);
            postGroup.computeIfAbsent("comments", k -> new ArrayList<Map<String, Object>>());
            ((List<Map<String, Object>>) postGroup.get("comments")).add(comment);
        }

        // 최신 글이 위로 오게 정렬 (post_id 큰 게 최신글)
        List<Map<String, Object>> groupedList = new ArrayList<>(grouped.values());

        // 페이징 계산
        int totalPosts = groupedList.size();
        int totalPages = (int) Math.ceil((double) totalPosts / size);
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalPosts);

        // 페이지에 맞는 게시글만 자르기
        List<Map<String, Object>> pagedList = groupedList.subList(start, end);

        // displayIndex: 최신글이 가장 큰 번호로 표시 (페이지 내에서 위->아래 순서)
        int displayIndex = totalPosts - start;
        for (Map<String, Object> postGroup : pagedList) {
            postGroup.put("displayIndex", displayIndex--);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", pagedList);
        result.put("totalPosts", totalPosts);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);

        return result;
    }

}
