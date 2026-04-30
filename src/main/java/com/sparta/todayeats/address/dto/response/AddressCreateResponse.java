package com.sparta.todayeats.address.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "배송지 등록 응답")
@Getter
@Builder
public class AddressCreateResponse {
    @Schema(description = "배송지 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID addressId;

    @Schema(description = "사용자 ID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID userId;

    @Schema(description = "별칭", example = "우리집")
    private String alias;

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 311")
    private String address;

    @Schema(description = "상세 주소", example = "3층")
    private String detail;

    @Schema(description = "우편번호", example = "06151")
    private String zipCode;

    @Schema(description = "기본 배송지 여부", example = "true")
    private boolean isDefault;

    @Schema(description = "생성 시간", example = "2026-04-30T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "생성자 ID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID createdBy;
}