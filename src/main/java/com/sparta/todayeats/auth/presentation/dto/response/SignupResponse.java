package com.sparta.todayeats.auth.presentation.dto.response;

import com.sparta.todayeats.user.domain.entity.User;
import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SignupResponse {
    private final String email;
    private final String nickname;
    private final UserRoleEnum role;
    private final LocalDateTime createdAt;
    private final UUID createdBy;

    public SignupResponse(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.createdBy = user.getCreatedBy();
    }
}