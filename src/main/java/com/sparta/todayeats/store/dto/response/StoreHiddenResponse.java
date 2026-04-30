package com.sparta.todayeats.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "가게 숨김 처리 응답")
@Getter
@Builder
public class StoreHiddenResponse {
    @Schema(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID storeId;

    @Schema(description = "숨김 여부", example = "true")
    private Boolean isHidden;

    @Schema(description = "생성 시간", example = "2026-04-30T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
    private UUID createdBy;

    @Schema(description = "수정 시간", example = "2026-04-30T11:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "수정자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
    private UUID updatedBy;
}
