package com.sparta.todayeats.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "주문 거절 요청")
public record RejectOrderRequest(
        @Schema(description = "거절 사유", example = "재료가 모두 소진되었습니다.")
        @Size(max = 255, message = "거절 사유는 255자 이하여야 합니다.")
        String rejectReason
) {}