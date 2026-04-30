package com.sparta.todayeats.category.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "카테고리 생성 응답")
@Getter
@Builder
public class CategoryCreateResponse {
    @Schema(description = "카테고리 ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
    private UUID categoryId;

    @Schema(description = "카테고리 이름", example = "한식")
    private String name;

    @Schema(description = "생성 시간", example = "2026-04-30T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;
}
