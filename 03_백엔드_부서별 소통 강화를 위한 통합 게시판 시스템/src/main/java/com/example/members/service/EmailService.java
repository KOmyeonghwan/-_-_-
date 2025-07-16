package com.example.members.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;


@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String username; // SMTP 서버에서 사용할 이메일 주소

    @Value("${spring.mail.password}")
    private String password; // SMTP 서버의 비밀번호

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 일반 이메일 발송 메서드
    public boolean sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(username); // 발신자
            helper.setTo(to); // 수신자
            helper.setSubject(subject); // 제목
            helper.setText(text, true); // 내용 (HTML 허용)

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 비밀번호 재설정 메일 발송 메서드
    public boolean sendPasswordResetEmail(String to, String resetLink) {
        String subject = "비밀번호 재설정";
        String text = "<p>안녕하세요. 아래 링크를 클릭하여 비밀번호를 재설정하세요:</p>" +
                      "<a href=\"" + resetLink + "\">비밀번호 재설정 링크</a>";

        return sendEmail(to, subject, text);
    }
}
