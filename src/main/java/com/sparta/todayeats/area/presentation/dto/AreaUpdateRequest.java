package com.sparta.todayeats.area.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 카테고리 수정 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AreaUpdateRequest {

    private String name;

    private String city;

    private String district;

    private Boolean isActive;
}
