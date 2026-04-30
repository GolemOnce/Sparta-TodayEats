package com.sparta.todayeats.user.dto.response;

import com.sparta.todayeats.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class UpdateUserResponse {
    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID userId;

    @Schema(description = "닉네임", example = "홍길동")
    private final String nickname;

    @Schema(description = "정보 공개 여부", example = "false")
    private final boolean visible;

    @Schema(description = "수정 시간", example = "2026-04-30T15:20:00")
    private final LocalDateTime updatedAt;

    public UpdateUserResponse(User user) {
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.visible = user.isPublic();
        this.updatedAt = user.getUpdatedAt();
    }
}