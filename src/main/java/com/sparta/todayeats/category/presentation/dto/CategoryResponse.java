package com.sparta.todayeats.category.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CategoryResponse {

    private UUID categoryId;
    private String name;
    private LocalDateTime createdAt;
    private UUID createdBy;
}
