package com.example.members.service;

import com.example.members.entity.Department;
import com.example.board.entity.BoardManager;
import com.example.board.repository.BoardManagerRepository;
import com.example.members.repository.DepartmentRepository;
import com.example.members.repository.UsersRepository;

import org.springframework.transaction.annotation.Transactional;

import com.example.board.service.BoardManagerService;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UsersRepository usersRepository;
    private final BoardManagerRepository boardManagerRepository;
    private final BoardManagerService boardManagerService;

    public DepartmentService(DepartmentRepository departmentRepository,
            UsersRepository usersRepository,
            BoardManagerRepository boardManagerRepository,
            BoardManagerService boardManagerService) {
        this.departmentRepository = departmentRepository;
        this.usersRepository = usersRepository;
        this.boardManagerRepository = boardManagerRepository;
        this.boardManagerService = boardManagerService;
    }

    public List<Department> findAll() {
        return departmentRepository.findAllByOrderByDeptNameAsc();
    }

    public Department findById(Integer deptId) {
        return departmentRepository.findById(deptId).orElse(null);
    }

    public Department save(Department department, boolean createBoard) {
        if (department.getCreatedAt() == null) {
            department.setCreatedAt(LocalDate.now());
        }

        // 자동 게시판 생성 옵션이 체크된 경우
        if (createBoard && department.getDeptCode() != null && !department.getDeptCode().isBlank()) {
            String boardCode = department.getDeptCode();
            String boardName = department.getDeptName();
            boardManagerService.createBoard(boardCode, boardName, department.getDeptCode());
        }

        return departmentRepository.save(department);
    }

    public void delete(Integer deptId) {
        Department department = departmentRepository.findById(deptId).orElse(null);
        if (department == null)
            return;

        String deptCode = department.getDeptCode();
        if (deptCode != null) {
            List<BoardManager> boards = boardManagerRepository.findByDeptCode(deptCode);
            for (BoardManager board : boards) {
                // ✅ 게시판 전체 삭제 (테이블 포함)
                boardManagerService.deleteBoard(board.getBoardCode());
            }
        }

        departmentRepository.deleteById(deptId);
    }

    public void updateMemberCount(Integer deptId) {
        Long count = departmentRepository.countUsersByDeptId(deptId);
        Department department = departmentRepository.findById(deptId).orElse(null);
        if (department != null) {
            department.setMemberCount(count.intValue());
            departmentRepository.save(department);
        }
    }

    public List<Map<String, Object>> findEmployeesByDepartmentId(Integer deptId) {
        return usersRepository.findEmployeesByDepartmentId(deptId);
    }

    @Transactional
    public void updateDepartmentName(Integer deptId, String newDeptName) {
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));

        // 부서 이름 변경
        dept.setDeptName(newDeptName);
        departmentRepository.save(dept);

        // 관련 게시판 이름도 함께 변경
        String deptCode = dept.getDeptCode();
        if (deptCode != null) {
            List<BoardManager> boards = boardManagerRepository.findByDeptCode(deptCode);
            for (BoardManager board : boards) {
                board.setBoardName(newDeptName + " 게시판");
                boardManagerRepository.save(board);
            }
        }
    }
}
