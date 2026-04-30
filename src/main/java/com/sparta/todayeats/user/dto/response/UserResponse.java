package com.sparta.todayeats.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.todayeats.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // NULL 필드 숨김
public class UserResponse {
    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID userId;

    @Schema(description = "이메일", example = "user01@example.com")
    private final String email;

    @Schema(description = "닉네임", example = "홍길동")
    private final String nickname;

    @Schema(description = "권한", example = "MANAGER")
    private final String role;

    @Schema(description = "정보 공개 여부", example = "false")
    private final boolean visible;

    @Schema(description = "생성 시간", example = "2026-04-30T15:00:00")
    private final LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2026-04-30T15:20:00")
    private final LocalDateTime updatedAt;

    @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Schema(description = "수정자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;

    @Schema(description = "삭제 시간", example = "2026-04-30T15:20:00")
    private LocalDateTime deletedAt;

    @Schema(description = "삭제자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID deletedBy;

    public UserResponse(User user, boolean isAdmin) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole().name();
        this.visible = user.isPublic();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();

        if (isAdmin) {
            this.createdBy = user.getCreatedBy();
            this.updatedBy = user.getUpdatedBy();
            this.deletedAt = user.getDeletedAt();
            this.deletedBy = user.getDeletedBy();
        }
    }
}