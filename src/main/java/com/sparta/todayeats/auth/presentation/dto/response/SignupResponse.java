package com.sparta.todayeats.auth.presentation.dto.response;

import com.sparta.todayeats.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SignupResponse {
    private final String email;
    private final String nickname;
    private final String role;
    private final LocalDateTime createdAt;

    public SignupResponse(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole().name();
        this.createdAt = user.getCreatedAt();
    }
}