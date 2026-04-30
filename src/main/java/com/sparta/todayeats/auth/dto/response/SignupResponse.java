package com.sparta.todayeats.auth.dto.response;

import com.sparta.todayeats.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "회원가입 응답")
@Getter
public class SignupResponse {
    @Schema(description = "이메일", example = "user01@example.com")
    private final String email;

    @Schema(description = "닉네임", example = "홍길동")
    private final String nickname;

    @Schema(description = "권한", example = "CUSTOMER")
    private final String role;

    @Schema(description = "생성 시간", example = "2026-04-30T10:00:00")
    private final LocalDateTime createdAt;

    public SignupResponse(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole().name();
        this.createdAt = user.getCreatedAt();
    }
}