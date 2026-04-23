package com.sparta.todayeats.category.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 카테고리 생성 요청 DTO
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}
