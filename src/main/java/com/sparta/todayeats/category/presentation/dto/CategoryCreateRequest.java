package com.sparta.todayeats.category.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CategoryCreateRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}
