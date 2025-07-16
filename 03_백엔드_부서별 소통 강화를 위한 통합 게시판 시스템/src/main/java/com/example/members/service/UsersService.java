package com.example.members.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.example.members.entity.Users;
import com.example.members.repository.UsersRepository;
import jakarta.transaction.Transactional;


@Service
public class UsersService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private DepartmentService departmentService;

    public void makeUser(Users user) { // 사용자를 만드는거
        usersRepository.save(user); // 유저라는 사용자가 넘어오면 그 사용자의 정보 저장
        if (user.getDepartment() != null) {
            departmentService.updateMemberCount(user.getDepartment().getDeptId()); // 부서 memberCount 업데이트
        }
    }

    public boolean isUseridTaken(String userid) { // 아이디가 있는지 확인하는거 있으면 true 없으면 false
        return usersRepository.existsByUserid(userid);
    }

    public Users findByUserid(String userid) { // userid를 사용해서 해당 사용자의 정보 찾는거
        return usersRepository.findByUserid(userid); // userid에 해당하는 사용자 정보를 반환
    }

    public List<Users> findAllUsers() { // 사용자 정보를 조회하는거
        return usersRepository.findAll();
    }

    public Page<Users> findAllUsers(Pageable pageable) {
        return usersRepository.findAll(pageable); // 페이징 처리
    }

    // 검색을 통해 사용자 리스트 반환
    public Page<Users> findBySearch(String search, Pageable pageable) {
        return usersRepository.findByUseridContaining(search, pageable);
    }

    public Page<Users> searchUsers(String searchType, String keyword, Pageable pageable) {
        if ("userid".equalsIgnoreCase(searchType)) {
            return usersRepository.findByUseridContainingIgnoreCase(keyword, pageable); // 대소문자 구분 없는 검색
        } else if ("username".equalsIgnoreCase(searchType)) {
            return usersRepository.findByUserNameContainingIgnoreCase(keyword, pageable); // 대소문자 구분 없는 검색
        } else {
            throw new IllegalArgumentException("Invalid search type: " + searchType); // 예외 처리
        }
    }

    @Transactional
    public void deleteUserByUserid(String userid) {
        Users user = usersRepository.findByUserid(userid);
        if (user != null) {
            Integer deptId = user.getDepartment().getDeptId(); // 삭제 전 부서 ID 확보
            usersRepository.deleteByUserid(userid); // 삭제
            departmentService.updateMemberCount(deptId); // memberCount 업데이트
        }
    }

    public Users findByNameAndPhone(String name, String phone) {
        return usersRepository.findByUserNameAndPhone(name, phone);
    }

    public Users findByUseridAndName(String userid, String name) {
        return usersRepository.findByUseridAndUserName(userid, name);
    }

    // 비밀번호 재설정 토큰 생성 및 저장
    public String createPasswordResetToken(String userid) {
        Users user = usersRepository.findByUserid(userid);
        if (user != null) {
            // 토큰 생성
            String resetToken = UUID.randomUUID().toString();
            // 토큰 유효 기간 설정 (예: 1시간)
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

            // 사용자 엔티티에 토큰과 만료일 추가
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(expiryDate);

            usersRepository.save(user);

            String resetLink = "https://yourdomain.com/reset-password?token=" + resetToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink); // 이메일 전송 메서드 호출

            return resetToken; // 생성된 토큰 반환
        }
        return null; // 사용자가 없으면 null 반환
    }

    // 비밀번호 재설정 토큰 확인 및 토큰 만료일 체크
    public boolean isResetTokenValid(String resetToken) {
        Optional<Users> userOptional = usersRepository.findByResetToken(resetToken);
        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            // 토큰이 만료되지 않았으면 유효
            if (user.getResetTokenExpiry().isAfter(LocalDateTime.now())) {
                return true;
            }
        }
        return false; // 토큰이 없거나 만료된 경우 false
    }

    // 새 비밀번호로 업데이트
    @Transactional
    public boolean resetPassword(String resetToken, String newPassword) {
        Optional<Users> userOptional = usersRepository.findByResetToken(resetToken);
        if (userOptional.isPresent()) {
            Users user = userOptional.get();

            // 비밀번호 업데이트
            user.setPassword(newPassword);
            // 토큰 삭제 (또는 유효하지 않게 설정)
            user.setResetToken(null);
            user.setResetTokenExpiry(null);

            usersRepository.save(user);
            return true; // 비밀번호 변경 성공
        }
        return false; // 유효하지 않은 토큰일 경우 false 반환
    }

    @Transactional // 수정된 부분
    public boolean updateUserInfo(String userid, String userName, String userEmail, String phone, String password,
            String newPassword) {
        Users user = usersRepository.findByUserid(userid); // 사용자 정보 조회

        if (user != null) {
            // 비밀번호가 일치하는지 확인
            if (!user.getPassword().equals(password)) {
                return false; // 비밀번호 불일치 시 처리
            }

            // 새 비밀번호가 있으면 변경
            if (newPassword != null && !newPassword.isBlank()) {
                user.setPassword(newPassword);
            }

            // 수정된 정보 업데이트
            user.setUserName(userName);
            user.setUserEmail(userEmail);
            user.setPhone(phone);

            // 변경된 정보 저장
            usersRepository.save(user);
            return true; // 업데이트 성공
        }

        return false; // 사용자 정보가 없으면 false
    }

    private final JdbcTemplate jdbcTemplate;

    
    public UsersService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

     public long countTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    // 오늘 가입한 사용자 수
    public long countTodayUsers() {
        LocalDate today = LocalDate.now();
        String start = today.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = today.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String sql = "SELECT COUNT(*) FROM users WHERE reg_date >= ? AND reg_date < ?";

        return jdbcTemplate.queryForObject(sql, Long.class, start, end);
    }



    

}
