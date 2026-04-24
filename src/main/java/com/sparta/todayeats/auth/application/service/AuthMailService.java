package com.sparta.todayeats.auth.application.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthMailService {
    private final JavaMailSender mailSender;

    // 이메일 인증번호 전송
    public void sendSignupCode(String email, String code) {
        sendEmail(
                email,
                "[TodayEats] 회원가입 인증 코드",
                buildSignupEmail(code)
        );
    }

    // 비밀번호 재설정 링크 전송
    public void sendResetPasswordLink(String email, String resetLink) {
        sendEmail(
                email,
                "[TodayEats] 비밀번호 재설정 안내",
                buildResetPasswordEmail(resetLink)
        );
    }

    // 공통 이메일 전송
    private void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }

    // 이메일 인증 템플릿
    private String buildSignupEmail(String code) {
        String template = loadTemplate();

        return template
                .replace("${title}", "회원가입 인증 코드")
                .replace("${description}", "아래 인증번호를 입력하여 회원가입을 완료해주세요.")
                .replace("${code}", code)
                .replace("${resetLink}", "");
    }

    // 비밀번호 재설정 템플릿
    private String buildResetPasswordEmail(String resetLink) {
        String template = loadTemplate();

        return template
                .replace("${title}", "비밀번호 재설정 안내")
                .replace("${description}", "아래 버튼을 클릭하여 비밀번호를 재설정하세요.")
                .replace("${code}", "")
                .replace("${resetLink}", resetLink);
    }

    private String loadTemplate() {
        try (InputStream inputStream =
                     new ClassPathResource("templates/todayeats-auth-email.html").getInputStream()
        ) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("템플릿 로딩 실패", e);
        }
    }
}