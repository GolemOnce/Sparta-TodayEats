package com.sparta.todayeats.category.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 카테고리 수정 요청 DTO
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateRequest {

    private String name;
}
