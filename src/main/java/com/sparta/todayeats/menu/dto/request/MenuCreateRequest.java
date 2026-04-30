package com.sparta.todayeats.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "메뉴 생성 요청")
public record MenuCreateRequest(
        @Schema(description = "메뉴 이름", example = "고기듬뿍 고향만두")
        @NotBlank(message = "메뉴 이름은 필수입니다.")
        @Size(max = 100, message = "메뉴 이름은 100자 이하입니다.")
        String name,

        @Schema(description = "가격", example = "5500")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Long price,

        @Schema(description = "설명", example = "국산 돼지고기와 신선한 야채가 어우러진 육즙 가득 만두")
        @Size(max = 500, message = "설명은 500자 이하입니다.")
        String description,

        @Schema(description = "이미지 URL", example = "https://image.example.com/mandoo.jpg")
        @Size(max = 255, message = "이미지 URL은 255자 이하입니다.")
        String imageUrl,

        @Schema(description = "카테고리 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "카테고리 ID는 필수입니다.")
        UUID categoryId,

        @Schema(description = "AI 설명 자동 생성 여부", example = "true")
        boolean aiDescription
) {
}