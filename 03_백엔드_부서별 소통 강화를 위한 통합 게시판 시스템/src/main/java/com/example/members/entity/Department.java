package com.example.members.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deptId;

    @Column(length = 50, nullable = false)
    private String deptName;

    @Column(nullable = false)
    private Integer memberCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(nullable = true)
    private String deptCode;
}
