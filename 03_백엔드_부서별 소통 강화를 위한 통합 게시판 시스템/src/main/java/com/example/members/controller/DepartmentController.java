package com.example.members.controller;

import com.example.members.entity.Department;
import com.example.members.entity.Users;
import com.example.members.repository.DepartmentRepository;
import com.example.members.service.DepartmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping("/list")
    public String listDepartments(HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getAdmin() != 1) {
            return "redirect:/login?redirect=/dashboard"; // 관리자 아니면 로그인으로 이동
        }
        
        // 부서 목록을 가져와서 Model에 추가
        model.addAttribute("departments", departmentService.findAll()); // findAll()은 부서 목록을 반환하는 서비스 메서드
        return "members/list"; // list.html을 반환
    }

    @PostMapping("/add")
    public String addDepartment(@RequestBody Map<String, Object> requestData) {
        String deptName = (String) requestData.get("deptName");
        String deptCode = (String) requestData.get("deptCode"); // 예: 부서명 기반 코드 자동 생성
        boolean autoCreateBoard = (boolean) requestData.get("autoCreateBoard");

        if (departmentRepository.findByDeptName(deptName).isPresent()
                || departmentRepository.findByDeptCode(deptCode).isPresent()) {
            return "<script>location.href='/admin/department/list';</script>";
        }

        Department newDepartment = new Department();
        newDepartment.setDeptName(deptName);
        newDepartment.setDeptCode(deptCode);
        newDepartment.setMemberCount(0);
        newDepartment.setCreatedAt(LocalDate.now());

        departmentService.save(newDepartment, autoCreateBoard);
        return "redirect:/admin/department/list";
    }

    // 부서 수정 처리 (get)
    @GetMapping("/get/{deptId}")
    @ResponseBody
    public Department getDepartment(@PathVariable("deptId") Integer deptId) {
        Department department = departmentService.findById(deptId);
        if (department != null) {
            return department;
        } else {
            return null; // 부서가 없을 경우 null 반환
        }
    }

    // 부서 수정 처리 (POST)
    @PostMapping("/update/{deptId}")
    @ResponseBody
    public ResponseEntity<String> updateDepartment(
            @PathVariable("deptId") Integer deptId,
            @RequestBody Map<String, String> request) {

        String deptName = request.get("deptName");

        // 중복 부서명 확인
        if (departmentRepository.findByDeptName(deptName).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409 상태코드
                    .body("이미 존재하는 부서입니다.");
        }

        // 정상 처리
        departmentService.updateDepartmentName(deptId, deptName);
        return ResponseEntity.ok("부서가 수정되었습니다.");
    }

    // 부서 삭제 처리
    @PostMapping("/delete/{deptId}")
    public String deleteDepartment(@PathVariable("deptId") Integer deptId) {
        departmentService.delete(deptId);
        return "redirect:/admin/department/list"; // 삭제 후 목록 페이지로 리다이렉트
    }

    // 부서 직원 목록
    @GetMapping("/employees/{deptId}")
    @ResponseBody
    public List<Map<String, Object>> getEmployeesByDepartment(@PathVariable("deptId") Integer deptId) {
        return departmentService.findEmployeesByDepartmentId(deptId);
    }
}
