package com.sparta.todayeats.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 가게 생성 요청
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateRequest {

    @NotBlank(message = "가게 이름은 필수입니다")
    private String name;

    @NotBlank(message = "주소는 필수입니다")
    private String address;

    private String phone;

    @NotNull(message = "운영 지역은 필수입니다")
    private UUID areaId;

    @NotNull(message = "카테고리는 필수입니다")
    private UUID categoryId;
}
