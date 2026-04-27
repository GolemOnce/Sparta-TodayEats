package com.sparta.todayeats.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.todayeats.user.domain.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // NULL 필드 숨김
public class UserResponse {
    private final UUID userId;
    private final String email;
    private final String nickname;
    private final String role;
    private final boolean visible;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime deletedAt;
    private UUID deletedBy;

    public UserResponse(User user, boolean isAdminUser) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole().name();
        this.visible = user.isPublic();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();

        if (isAdminUser) {
            this.createdBy = user.getCreatedBy();
            this.updatedBy = user.getUpdatedBy();
            this.deletedAt = user.getDeletedAt();
            this.deletedBy = user.getDeletedBy();
        }
    }
}