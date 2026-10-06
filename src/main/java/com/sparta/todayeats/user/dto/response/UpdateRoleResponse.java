package com.sparta.todayeats.user.dto.response;

import com.sparta.todayeats.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "사용자 권한 변경 응답")
@Getter
public class UpdateRoleResponse {
    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID userId;

    @Schema(description = "권한", example = "MANAGER")
    private final String role;

    @Schema(description = "수정 시간", example = "2026-04-30T15:20:00")
    private final LocalDateTime updatedAt;

    public UpdateRoleResponse(User user) {
        this.userId = user.getUserId();
        this.role = user.getRole().name();
        this.updatedAt = user.getUpdatedAt();
    }
}