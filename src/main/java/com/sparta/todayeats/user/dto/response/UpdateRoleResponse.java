package com.sparta.todayeats.user.dto.response;

import com.sparta.todayeats.user.domain.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class UpdateRoleResponse {
    private final UUID userId;
    private final String role;
    private final LocalDateTime updatedAt;

    public UpdateRoleResponse(User user) {
        this.userId = user.getUserId();
        this.role = user.getRole().name();
        this.updatedAt = user.getUpdatedAt();
    }
}