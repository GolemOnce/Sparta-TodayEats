package com.sparta.todayeats.user.dto.response;

import com.sparta.todayeats.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "사용자 삭제 응답")
@Getter
public class DeleteUserResponse {
    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID userId;

    @Schema(description = "삭제 시간", example = "2026-04-30T15:20:00")
    private final LocalDateTime deletedAt;

    public DeleteUserResponse(User user) {
        this.userId = user.getUserId();
        this.deletedAt = user.getDeletedAt();
    }
}