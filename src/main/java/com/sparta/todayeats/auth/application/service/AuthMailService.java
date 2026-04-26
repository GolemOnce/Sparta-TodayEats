package com.sparta.todayeats.auth.application.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class AuthMailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.url.base}")
    private String baseUrl;

    @Value("${app.url.password-reset-path}")
    private String resetPath;

    // 이메일 인증번호 전송
    public void sendSignupCode(String email, String code) {
        Context context = new Context();
        context.setVariable("title", "회원가입 인증번호");
        context.setVariable("description", "아래 인증번호를 입력하여 회원가입을 완료해주세요.");
        context.setVariable("code", code);

        sendEmail(
                email,
                "[TodayEats] 회원가입 인증번호",
                templateEngine.process("todayeats-auth-email", context)
        );
    }

    // 비밀번호 재설정 링크 전송
    public void sendPasswordResetLink(String email, String code) {
        Context context = new Context();
        context.setVariable("title", "비밀번호 재설정 안내");
        context.setVariable("description", "아래 버튼을 클릭하여 새로운 비밀번호를 설정하세요.");
        context.setVariable("resetLink", baseUrl + resetPath + "?code=" + code);

        sendEmail(
                email,
                "[TodayEats] 비밀번호 재설정 안내",
                templateEngine.process("todayeats-auth-email", context)
        );
    }

    // 공통 이메일 전송
    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }
}