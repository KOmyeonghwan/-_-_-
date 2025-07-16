package com.example.main;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.board.entity.BoardManager;
import com.example.board.service.BoardManagerService;

@Controller
public class MainController {

        @Autowired
        private BoardManagerService boardManagerService;

        @GetMapping("/main")
        public String showMainPage(Model model, @ModelAttribute("fixedBoards") List<BoardManager> fixedBoards) {
                List<Map<String, Object>> fixedBoardList = new ArrayList<>();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                for (BoardManager board : fixedBoards) {
                        List<Map<String, Object>> rawPosts = boardManagerService.LatestPosts(board.getBoardCode(), 5);
                        List<Map<String, Object>> posts = new ArrayList<>();

                        for (Map<String, Object> post : rawPosts) {
                                Map<String, Object> formattedPost = new HashMap<>(post);

                                Object dateObj = post.get("date");
                                if (dateObj instanceof Timestamp) {
                                        Timestamp timestamp = (Timestamp) dateObj;
                                        String formattedDate = timestamp.toLocalDateTime().format(formatter);
                                        formattedPost.put("date", formattedDate);
                                } else if (dateObj != null) {
                                        formattedPost.put("date", dateObj.toString()); // fallback
                                }
                                
                                formattedPost.put("boardCode", board.getBoardCode());  

                                posts.add(formattedPost);
                        }

                        Map<String, Object> boardData = new HashMap<>();
                        boardData.put("boardId", board.getId());
                        boardData.put("boardName", board.getBoardName());
                        boardData.put("posts", posts); // 날짜 포함된 게시글 리스트

                        fixedBoardList.add(boardData);
                }

                model.addAttribute("fixedBoardList", fixedBoardList);
                return "main";
        }

        @GetMapping("/mypage-content")
        public String getMyPageContent() {
                return "fragments/user/mypage"; // 이미 GlobalModelAttribute에서 loginUser가 들어감
        }

}
