package com.sparta.todayeats.area.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 운영 지역 생성 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AreaCreateRequest {

    @NotBlank(message = "지역명은 필수입니다.")
    private String name;

    @NotBlank(message = "시/도는 필수입니다.")
    private String city;

    @NotBlank(message = "구/군은 필수입니다.")
    private String district;
}
