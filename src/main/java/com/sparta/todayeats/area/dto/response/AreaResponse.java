package com.sparta.todayeats.area.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "지역 정보 응답")
@Getter
@Builder
public class AreaResponse {
    @Schema(description = "지역 ID", example = "770e8400-e29b-41d4-a716-446655440001")
    private UUID areaId;

    @Schema(description = "지역 이름", example = "광화문")
    private String name;

    @Schema(description = "시/도", example = "서울특별시")
    private String city;

    @Schema(description = "구/군", example = "종로구")
    private String district;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "생성 시간", example = "2026-04-30T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "생성자 ID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID createdBy;

    @Schema(description = "수정 시간", example = "2026-04-30T12:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "수정자 ID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID updatedBy;
}