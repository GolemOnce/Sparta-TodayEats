package com.sparta.todayeats.user.dto.response;

import com.sparta.todayeats.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DeleteUserResponse {
    private final UUID userId;
    private final LocalDateTime deletedAt;

    public DeleteUserResponse(User user) {
        this.userId = user.getUserId();
        this.deletedAt = user.getDeletedAt();
    }
}