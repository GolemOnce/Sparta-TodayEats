package com.sparta.todayeats.user.dto.response;

import com.sparta.todayeats.user.domain.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class UpdatePasswordResponse {
    private final UUID userId;
    private final LocalDateTime updatedAt;

    public UpdatePasswordResponse(User user) {
        this.userId = user.getUserId();
        this.updatedAt = user.getUpdatedAt();
    }
}