package com.sparta.todayeats.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MenuCreateRequest(

        @NotBlank(message = "메뉴 이름은 필수입니다.")
        @Size(max = 100, message = "메뉴 이름은 100자 이하입니다.")
        String name,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Long price,

        @Size(max = 500, message = "설명은 500자 이하입니다.")
        String description,

        @Size(max = 255, message = "이미지 URL은 255자 이하입니다.")
        String imageUrl,

        @NotNull(message = "카테고리 ID는 필수입니다.")
        UUID categoryId
) {
}