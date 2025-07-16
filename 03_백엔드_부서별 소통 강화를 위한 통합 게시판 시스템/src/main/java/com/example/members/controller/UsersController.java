package com.example.members.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.example.board.service.BoardManagerService;
import com.example.members.entity.Department;
import com.example.members.entity.Note;
import com.example.members.entity.Users;
import com.example.members.repository.DepartmentRepository;
import com.example.members.service.UsersService;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;

@Controller
public class UsersController {

    @Autowired
    private BoardManagerService boardManagerService;

    @Autowired
    private NoteController noteController;

    @Autowired
    private UsersService usersService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "redirect", required = false) String redirect, Model model) {
        model.addAttribute("redirect", redirect != null ? redirect : "");
        return "members/login";
    }

    @GetMapping("/findid")
    public String showForgotIdPage() {
        return "members/findid";
    }

    @GetMapping("/mypage")
    public String showMyPage(HttpSession session, Model model) {
        Users loginUser = (Users) session.getAttribute("user");
        if (loginUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("loginUser", loginUser);
        return "members/mypage"; // members/mypage.html 로 이동
    }

    @GetMapping("/admin/department")
    public String showDepartmentList(HttpSession session, Model model) {
        Users loggedInUser = (Users) session.getAttribute("user");

        // 로그인된 사용자 정보가 없거나 관리자 권한이 아닐 경우
        if (loggedInUser == null || loggedInUser.getAdmin() != 1) {
            return "redirect:/login?redirect=/dashboard"; // 로그인 페이지로 리디렉션
        }

        List<Department> departments = departmentRepository.findAll();
        model.addAttribute("departments", departments);
        return "members/list"; // 부서 목록 페이지로 이동
    }

    @GetMapping("/findpwd")
    public String showForgotPWD() {
        return "members/findpwd";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        List<Department> departments = departmentRepository.findAll();
        model.addAttribute("departments", departments);
        model.addAttribute("users", new Users()); // 폼 바인딩용

        return "members/register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user"); // 세션에서 사용자 제거
        return "redirect:/main";
    }

    @GetMapping("/dashboard")
    public String showDashBoard(HttpSession session, Model model) {
        Users loggedInUser = (Users) session.getAttribute("user");

        // 로그인된 사용자 정보가 없거나 관리자 권한이 아닐 경우
        if (loggedInUser == null || loggedInUser.getAdmin() != 1) {
            return "redirect:/login?redirect=/dashboard"; // 로그인 페이지로 리디렉션
        }

        // 대시보드 데이터를 가져오기 위한 서비스 호출
        long totalBoards = boardManagerService.countTotalBoards();
        long totalPosts = boardManagerService.countTotalPosts();
        long totalUsers = usersService.countTotalUsers();
        List<Map<String, Object>> latestPosts = boardManagerService.getLatestPostsForAllBoards(); // 동적으로 최신 게시글 가져오기
        long todayPosts = boardManagerService.countTodayPosts();
        long todayUsers = usersService.countTodayUsers();

        // 게시판별 데이터 필터링 (suggest, notice, free)
        List<Map<String, Object>> suggestList = new ArrayList<>();
        List<Map<String, Object>> noticeList = new ArrayList<>();
        List<Map<String, Object>> freeList = new ArrayList<>();

        for (Map<String, Object> post : latestPosts) {
            String boardCode = (String) post.get("boardCode");

            if ("suggest".equals(boardCode)) {
                suggestList.add(post);
            } else if ("notice".equals(boardCode)) {
                noticeList.add(post);
            } else if ("free".equals(boardCode)) {
                freeList.add(post);
            }
        }

        // 모델에 데이터를 추가
        model.addAttribute("totalBoards", totalBoards);
        model.addAttribute("totalPosts", totalPosts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("todayPosts", todayPosts);
        model.addAttribute("todayUsers", todayUsers);
        model.addAttribute("suggestList", suggestList);
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("freeList", freeList);

        return "members/dashboard"; // 대시보드 페이지로 이동
    }

    @GetMapping("/admin")
    public String showAdminPage(
            @RequestParam(value = "type", required = false) String searchType,
            @RequestParam(value = "search", required = false) String search,
            HttpSession session,
            Model model,
            @PageableDefault(size = 10, sort = "useridx", direction = Sort.Direction.DESC) Pageable pageable) {

        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getAdmin() != 1) {
            return "redirect:/login?redirect=/dashboard"; // 관리자 아니면 로그인으로 이동
        }

        // 디버깅 코드 추가
        System.out.println("Search Type: " + searchType); // 검색 타입이 제대로 넘어왔는지 확인

        // 검색 타입 검증
        if (searchType != null && !searchType.equals("userid") && !searchType.equals("username")) {
            searchType = null; // 검색 타입이 잘못되었을 경우 null 처리
        }

        // 이름, 아이디 검색
        Page<Users> userPage;

        if (search != null && !search.isBlank() && searchType != null) {
            userPage = usersService.searchUsers(searchType, search, pageable);
        } else {
            userPage = usersService.findAllUsers(pageable);
        }

        // 사용자 정보 리스트 생성
        List<Map<String, Object>> userDataList = new ArrayList<>();
        int idxOffset = userPage.getNumber() * userPage.getSize(); // 현재 페이지에서의 첫 번째 인덱스 오프셋

        // 가장 오래된 회원이 먼저 오도록 순회
        List<Users> usersList = userPage.getContent(); // 그대로 순차적으로 진행

        for (int i = 0; i < usersList.size(); i++) {
            Users u = usersList.get(i);
            Map<String, Object> map = new HashMap<>();

            // 번호는 가장 오래된 회원이 1번부터 시작하도록 설정
            map.put("useridx", idxOffset + usersList.size() - i); // 오래된 회원이 1번 번호를 가짐
            map.put("userid", u.getUserid());
            map.put("userName", u.getUserName());
            map.put("userEmail", u.getUserEmail());
            map.put("zipCode", u.getZipCode());
            map.put("phone", formatPhoneNumber(u.getPhone()));
            map.put("reg_date", u.getReg_date());
            map.put("admin", u.getAdmin());
            map.put("isAdmin", u.getAdmin() == 1);

            // 부서 정보 추가
            if (u.getDepartment() != null) {
                map.put("deptName", u.getDepartment().getDeptName()); // 부서 이름 추가
            } else {
                map.put("deptName", "-"); // 부서 정보가 없다면 "-" 처리
            }

            userDataList.add(map);
        }
        // 페이지 정보 구성
        List<Map<String, Object>> pageList = new ArrayList<>();
        int currentPage = userPage.getNumber();
        int totalPages = userPage.getTotalPages();

        for (int i = 0; i < totalPages; i++) {
            Map<String, Object> page = new HashMap<>();
            page.put("number", i);
            page.put("display", i + 1);
            page.put("active", i == currentPage);
            pageList.add(page);
        }

        Map<String, Object> userPageData = new HashMap<>();
        userPageData.put("hasPrevious", userPage.hasPrevious());
        userPageData.put("hasNext", userPage.hasNext());
        userPageData.put("prevPage", currentPage - 1);
        userPageData.put("nextPage", currentPage + 1);
        userPageData.put("pages", pageList);

        // 모델에 담기
        model.addAttribute("userList", userDataList);
        model.addAttribute("userPage", userPageData);
        model.addAttribute("loginUser", user);
        model.addAttribute("isUserId", "userid".equals(searchType));
        model.addAttribute("isUserName", "username".equals(searchType));
        model.addAttribute("search", search != null ? search : "");

        return "members/admin";
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null)
            return "";

        // 숫자만 남기기
        phone = phone.replaceAll("[^0-9]", "");

        if (phone.length() == 11) {
            // 예: 01012345678 → 010-1234-5678
            return phone.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        } else if (phone.length() == 10) {
            // 예: 0111234567 → 011-123-4567 또는 02 지역번호
            return phone.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        } else if (phone.length() == 9) {
            // 예: 02-123-4567 (서울 지역번호 2자리)
            return phone.replaceFirst("(\\d{2})(\\d{3})(\\d{4})", "$1-$2-$3");
        } else {
            // 알 수 없는 형식 → 그대로 반환
            return phone;
        }
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute Users users, Model model) {
        if (usersService.isUseridTaken(users.getUserid())) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "members/register";
        }

        if (users.getDepartment() == null || users.getDepartment().getDeptId() == null) {
            model.addAttribute("error", "부서 선택은 필수입니다.");
            return "members/register";
        }

        Optional<Department> optionalDept = departmentRepository.findById(users.getDepartment().getDeptId());
        if (optionalDept.isEmpty()) {
            model.addAttribute("error", "선택한 부서가 존재하지 않습니다.");
            return "members/register";
        }

        users.setDepartment(optionalDept.get());
        users.setAdmin(0);
        usersService.makeUser(users);

        return "redirect:/login";
    }

    @PostMapping("/find-id")
    @ResponseBody
    public Map<String, Object> findId(@RequestParam String name, @RequestParam String phone) {
        Map<String, Object> response = new HashMap<>();

        Users user = usersService.findByNameAndPhone(name, phone);
        if (user != null) {
            response.put("success", true);
            response.put("userid", user.getUserid());
        } else {
            response.put("success", false);
        }

        return response;
    }

    // 비밀번호 재설정 요청 (비밀번호 재설정 링크 이메일로 발송)
    @PostMapping("/reset-password-request")
    @ResponseBody
    public Map<String, Object> resetPasswordRequest(@RequestParam String userid) {
        Map<String, Object> response = new HashMap<>();

        // 비밀번호 재설정 토큰 생성
        String token = usersService.createPasswordResetToken(userid);
        if (token != null) {
            response.put("success", true);
            response.put("message", "비밀번호 재설정 링크가 이메일로 전송되었습니다.");
            // 실제 이메일 발송 코드 추가 필요
        } else {
            response.put("success", false);
            response.put("message", "해당 아이디는 존재하지 않습니다.");
        }

        return response;
    }

    @PostMapping("/mypage/update-info") // 수정 된부분
    public String updateUserInfo(@RequestParam String userName,
            @RequestParam String userEmail,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String rePassword,
            HttpSession session, Model model) {

        // 로그인한 사용자 정보 가져오기
        Users loggedInUser = (Users) session.getAttribute("user");

        if (loggedInUser != null) {
            // 비밀번호가 null일 경우와 일치 여부를 확인
            if (loggedInUser.getPassword() == null || !loggedInUser.getPassword().equals(password)) {
                model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
                return "members/mypage"; // 비밀번호 불일치 에러 메시지 출력
            }

            // 새 비밀번호 확인
            if (newPassword != null && !newPassword.isBlank()) {
                loggedInUser.setPassword(newPassword); // 새 비밀번호가 있을 경우 업데이트
            }

            // 사용자 정보 업데이트
            boolean isUpdated = usersService.updateUserInfo(
                    loggedInUser.getUserid(),
                    userName,
                    userEmail,
                    phone,
                    password,
                    newPassword);

            if (isUpdated) {
                // 수정이 성공하면 세션에 최신 정보 반영
                loggedInUser.setUserName(userName);
                loggedInUser.setUserEmail(userEmail);
                loggedInUser.setPhone(phone);
                if (newPassword != null && !newPassword.isBlank()) {
                    loggedInUser.setPassword(newPassword); // 새 비밀번호가 있을 경우 업데이트
                }
                session.setAttribute("user", loggedInUser); // 세션에 사용자 전체 정보 저장
                return "redirect:/mypage"; // 정보 수정 후 마이 페이지로 리디렉션
            } else {
                model.addAttribute("error", "정보 수정 중 오류가 발생했습니다.");
                return "members/mypage"; // 정보 수정 실패 시 오류 메시지 출력
            }
        }

        return "redirect:/login"; // 로그인하지 않았다면 로그인 페이지로 리디렉션
    }

    // 비밀번호 재설정 처리
    @PostMapping("/reset-password")
    @ResponseBody
    public Map<String, Object> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();

        // 토큰 유효성 검증 및 비밀번호 재설정
        boolean success = usersService.resetPassword(token, newPassword);
        if (success) {
            response.put("success", true);
            response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
        } else {
            response.put("success", false);
            response.put("message", "유효하지 않은 토큰입니다.");
        }

        return response;
    }

    @PostMapping("/login")
    @ResponseBody // json 응답처리
    public Map<String, Object> loginUser(
            @RequestParam("userid") String userid,
            @RequestParam("password") String password,
            @RequestParam(value = "redirect", required = false) String redirect,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        Users user = usersService.findByUserid(userid); // 아이디로 회원 조회

        if (user != null && user.getPassword().equals(password)) { // 아이디와 비번이 맞는경우
            user.setPassword(null); // 비밀번호 널 처리
            session.setAttribute("user", user); // 세션에 사용자 정보 저장
            response.put("success", true); // 로그인 처리

            // 로그인 후 이동할 페이지 설정
            if (redirect != null && !redirect.isBlank()) { // 이전에 보고 있던 페이지로
                response.put("redirect", redirect);
            } else if (user.getAdmin() == 1) {
                response.put("redirect", "/dashboard"); // 관리자페이지
            } else {
                response.put("redirect", "/main"); // 일반 사용자 메인 페이지
            }
        } else {
            response.put("success", false); // 로그인 실패
        }
        return response;
    }


    @PostMapping("/check-userid") // 아이디 중복확인 처리
    @ResponseBody // json 응답
    public boolean checkUserid(@RequestParam("userid") String userid) {
        return usersService.isUseridTaken(userid); // true or false로 처리
    }

    @PostMapping("/admin/delete/{userid}")
    @ResponseBody
    public String deleteUser(@PathVariable String userid, HttpSession session) {
        Users loginUser = (Users) session.getAttribute("user");

        // 관리자라면 삭제 금지
        if (loginUser != null && loginUser.getAdmin() == 1 && loginUser.getUserid().equals(userid)) {
            return "fail";
        }

        usersService.deleteUserByUserid(userid);
        return "success";
    }

    @PostMapping("/admin/update-admin")
    @ResponseBody
    public String updateAdmin(@RequestBody Map<String, Object> data, HttpSession session) {
        Users loginUser = (Users) session.getAttribute("user");

        if (loginUser == null || loginUser.getAdmin() != 1) {
            return "unauthorized"; // 관리자 아님
        }

        String userid = (String) data.get("userid");
        int admin = (int) data.get("admin");

        // 본인 권한 변경 막기
        if (loginUser.getUserid().equals(userid)) {
            return "forbidden"; // 본인 admin 권한은 변경 불가
        }

        Users user = usersService.findByUserid(userid);
        if (user != null) {
            user.setAdmin(admin);
            usersService.makeUser(user);
            return "success";
        } else {
            return "fail";
        }
    }


        // 쪽지
        // UsersController 내의 적절한 메서드에서 로그인한 사용자 정보 가져오기
        public String sendMessage(HttpSession session, @RequestParam String receiverId, @RequestParam String message) {
            Users loginUser = (Users) session.getAttribute("user");
            if (loginUser == null) {
                return "redirect:/login"; // 로그인하지 않았다면 로그인 페이지로 리디렉션
            }
        
            // NoteController에 쪽지 전송 요청
            Note requestNote = new Note();
            requestNote.setSenderId(loginUser.getUserid());
            requestNote.setReceiverId(receiverId);
            requestNote.setMessage(message);
        
            // NoteService를 통해 쪽지 전송 (NoteController에서 제공한 메서드 사용)
            ResponseEntity<Note> response = noteController.sendNote(requestNote, session);
        
            if (response.getStatusCode().is2xxSuccessful()) {
                return "redirect:/mypage"; // 쪽지 전송 성공 후 마이 페이지로 리디렉션
            } else {
                return "redirect:/send-fail"; // 실패 시 적절한 페이지로 이동
            }
        }


    // 메인 페이지 에이젝스 쪽지
    @GetMapping("/api/session-user")
    @ResponseBody
    public Users getSessionUser(HttpSession session) {
        Users loginUser = (Users) session.getAttribute("user");
    
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }
    
        loginUser.setPassword(null); // 🔒 보안: 비밀번호 제거
        return loginUser;
    }


}
