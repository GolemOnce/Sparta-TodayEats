package com.sparta.todayeats.user.dto.response;

import com.sparta.todayeats.user.domain.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class UpdateUserResponse {
    private final UUID userId;
    private final String nickname;
    private final boolean visible;
    private final LocalDateTime updatedAt;

    public UpdateUserResponse(User user) {
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.visible = user.isPublic();
        this.updatedAt = user.getUpdatedAt();
    }
}