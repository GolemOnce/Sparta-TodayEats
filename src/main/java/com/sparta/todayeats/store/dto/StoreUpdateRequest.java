package com.sparta.todayeats.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 가게 수정 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequest {

    @NotBlank(message = "가게 이름은 필수입니다")
    private String name;

    @NotBlank(message = "주소는 필수입니다")
    private String address;

    private String phone;
}
