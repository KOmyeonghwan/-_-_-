package com.example.members.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.members.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    @Query("SELECT COUNT(u) FROM Users u WHERE u.department.deptId = :deptId")
    Long countUsersByDeptId(@Param("deptId") Integer deptId);

    List<Department> findAllByOrderByDeptNameAsc(); // 부서명을 오름차순으로 정렬
    Optional<Department> findByDeptCode(String deptcode);
    Optional<Department> findByDeptName(String deptName);
}
