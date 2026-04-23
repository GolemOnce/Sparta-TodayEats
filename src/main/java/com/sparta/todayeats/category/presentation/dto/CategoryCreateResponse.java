package com.sparta.todayeats.category.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// 카테고리 생성 응답 DTO
@Getter
@Builder
public class CategoryCreateResponse {

    private UUID categoryId;
    private String name;

    private LocalDateTime createdAt;
    private UUID createdBy;
}
