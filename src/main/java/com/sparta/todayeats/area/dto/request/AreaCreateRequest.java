package com.sparta.todayeats.area.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "지역 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AreaCreateRequest {
    @Schema(description = "지역 이름", example = "광화문")
    @NotBlank(message = "지역 이름은 필수입니다.")
    private String name;

    @Schema(description = "시/도", example = "서울특별시")
    @NotBlank(message = "시/도는 필수입니다.")
    private String city;

    @Schema(description = "구/군", example = "종로구")
    @NotBlank(message = "구/군은 필수입니다.")
    private String district;
}
