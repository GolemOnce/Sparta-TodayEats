package com.sparta.todayeats.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "가게 수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequest {
    @Schema(description = "가게 이름", example = "맛있는 한식당")
    @NotBlank(message = "가게 이름은 필수입니다")
    @Size(max = 100, message = "가게 이름은 100자 이내입니다")
    private String name;

    @Schema(description = "가게 주소", example = "서울특별시 강남구 테헤란로 311")
    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 255, message = "주소는 255자 이내입니다")
    private String address;

    @Schema(description = "가게 전화번호", example = "02-9876-5432")
    @Size(max = 20, message = "전화번호는 20자 이내입니다")
    private String phone;
}
