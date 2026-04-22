package com.sparta.todayeats.category.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

// 카테고리 생성 요청 DTO
@Getter
public class CategoryCreateRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}
