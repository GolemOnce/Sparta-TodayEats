package com.sparta.todayeats.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 상태 변경 요청")
public record MenuStatusUpdateRequest(
        @Schema(description = "숨김 여부", example = "false")
        boolean isHidden,

        @Schema(description = "품절 여부", example = "true")
        boolean soldOut
) {
}