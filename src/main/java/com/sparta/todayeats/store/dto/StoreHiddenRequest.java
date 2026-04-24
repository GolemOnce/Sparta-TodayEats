package com.sparta.todayeats.store.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 가게 숨김 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreHiddenRequest {

    @NotNull(message = "숨김 여부는 필수입니다")
    private Boolean isHidden;
}
