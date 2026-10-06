package com.sparta.todayeats.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Schema(description = "가게 등록 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateRequest {
    @Schema(description = "가게 이름", example = "맛있는 한식당")
    @NotBlank(message = "가게 이름은 필수입니다")
    @Size(max = 100, message = "가게 이름은 100자 이내입니다")
    private String name;

    @Schema(description = "가게 주소", example = "서울특별시 강남구 테헤란로 311")
    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 255, message = "주소는 255자 이내입니다")
    private String address;

    @Schema(description = "가게 전화번호", example = "02-1234-5678")
    @Size(max = 20, message = "전화번호는 20자 이내입니다")
    private String phone;

    @Schema(description = "지역 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "운영 지역은 필수입니다")
    private UUID areaId;

    @Schema(description = "카테고리 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull(message = "카테고리는 필수입니다")
    private UUID categoryId;
}
