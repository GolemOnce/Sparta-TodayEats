package com.sparta.todayeats.address.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "배송지 등록 요청")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddressCreateRequest {
    @Schema(description = "별칭", example = "우리집")
    private String alias;

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 311")
    private String address;

    @Schema(description = "상세 주소", example = "3층")
    private String detail;

    @Schema(description = "우편번호", example = "06151")
    private String zipCode;
}
