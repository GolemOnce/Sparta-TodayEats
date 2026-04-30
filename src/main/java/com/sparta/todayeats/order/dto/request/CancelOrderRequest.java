package com.sparta.todayeats.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "주문 취소 요청")
public record CancelOrderRequest(
        @Schema(description = "취소 사유", example = "주문 정보를 잘못 입력했어요!")
        @Size(max = 255, message = "취소 사유는 255자 이하여야 합니다.")
        String cancelReason
) {}