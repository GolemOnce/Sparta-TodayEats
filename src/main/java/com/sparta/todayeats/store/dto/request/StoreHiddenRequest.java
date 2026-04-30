package com.sparta.todayeats.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "가게 숨김 처리 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreHiddenRequest {
    @Schema(description = "숨김 여부", example = "true")
    @NotNull(message = "숨김 여부는 필수입니다")
    private Boolean isHidden;
}
