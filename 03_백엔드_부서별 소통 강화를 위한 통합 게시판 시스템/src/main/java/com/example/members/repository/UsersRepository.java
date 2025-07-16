package com.example.members.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.members.entity.Department;
import com.example.members.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    boolean existsByUserid(String userid); // 아이디가 이미 있는지 확인하는거 중복확인

    Users findByUserid(String userid); // 이 아이디가를 가진 회원 찾는거

    void deleteByUserid(String userid); // 아이디 삭제

    Page<Users> findByUseridContaining(String userid, Pageable pageable);

    Page<Users> findByUserNameContaining(String userName, Pageable pageable);

    Page<Users> findByUseridContainingIgnoreCase(String userid, Pageable pageable); //대소문자 구분X

    Page<Users> findByUserNameContainingIgnoreCase(String userName, Pageable pageable); //대소문자 구분X

    int countByDepartment(Department department);

    Users findByUserNameAndPhone(String userName, String phone);

    Users findByUseridAndUserName(String userid, String userName);

    // resetToken을 기준으로 사용자를 찾는 메서드
    Optional<Users> findByResetToken(String resetToken);

    // resetTokenExpiry가 현재 시간보다 이후인 사용자를 찾는 메서드
    Optional<Users> findByResetTokenExpiryAfterAndResetToken(LocalDateTime resetTokenExpiry, String resetToken);

    // 부서 ID로 해당 부서에 속한 직원들의 이름과 아이디를 가져오는 쿼리
    @Query("SELECT new map(u.userName as userName, u.userid as userid) FROM Users u WHERE u.department.deptId = :deptId")
    List<Map<String, Object>> findEmployeesByDepartmentId(@Param("deptId") Integer deptId);

    long count(); // 전체 사용자 수를 카운트

}
