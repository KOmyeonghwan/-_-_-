package com.example.members.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer useridx;

    @Column(length = 50)
    private String userid;

    @Column(length = 100)
    private String password;

    @Column(length = 30, name = "user_name")
    private String userName;

    @Column(length = 150, name = "user_email")
    private String userEmail;

    @Column(length = 200, name = "zip_code")
    private String zipCode;

    @Column(length = 20)
    private String phone;

    @Column(name = "reg_date")
    @DateTimeFormat(pattern = "yyyy.MM.dd")
    private LocalDate reg_date = LocalDate.now();

    private int admin = 0;

    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;

    @Column(name = "reset_token", length = 100)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    public String getEmail() {
        return this.userEmail; // userEmail을 반환
    }
}
