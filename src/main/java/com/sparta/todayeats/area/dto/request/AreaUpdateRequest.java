package com.sparta.todayeats.area.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 운영 지역 수정 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AreaUpdateRequest {

    @NotBlank(message = "지역 이름은 필수입니다")
    private String name;

    @NotBlank(message = "시/도는 필수입니다")
    private String city;

    @NotBlank(message = "구/군은 필수입니다")
    private String district;

    @NotNull(message = "활성화 여부는 필수입니다")
    private Boolean isActive;
}