package com.sparta.todayeats.menu.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuUpdateRequest(

        @NotBlank(message = "메뉴 이름은 필수입니다.")
        @Size(max = 100, message = "메뉴 이름은 100자 이하입니다.")
        String name,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        int price,

        @Size(max = 500, message = "설명은 500자 이하입니다.")
        String description,

        @Size(max = 255, message = "이미지 URL은 255자 이하입니다.")
        String imageUrl
) {
}