package com.sparta.todayeats.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "가게 정보 응답")
@Getter
@Builder
public class StoreResponse {
    @Schema(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID storeId;

    @Schema(description = "가게 주인 닉네임", example = "홍길동")
    private String ownerNickname;

    @Schema(description = "가게 이름", example = "맛있는 한식당")
    private String name;

    @Schema(description = "가게 주소", example = "서울특별시 강남구 테헤란로 311")
    private String address;

    @Schema(description = "가게 전화번호", example = "02-1234-5678")
    private String phone;

    @Schema(description = "지역 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID areaId;

    @Schema(description = "지역 이름", example = "서울 강남구")
    private String areaName;

    @Schema(description = "카테고리 ID", example = "c10e8400-e29b-41d4-a716-446655443333")
    private UUID categoryId;

    @Schema(description = "카테고리 이름", example = "중식/만두")
    private String categoryName;

    @Schema(description = "평점 (1~5점)", example = "4.8")
    private BigDecimal averageRating;

    @Schema(description = "숨김 여부", example = "false")
    private Boolean isHidden;

    @Schema(description = "생성 시간", example = "2026-04-30T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
    private UUID createdBy;

    @Schema(description = "수정 시간", example = "2026-04-30T15:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "수정자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
    private UUID updatedBy;
}
