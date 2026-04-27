package com.sparta.todayeats.order.presentation.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 주문 취소 요청 DTO
 */
public record CancelOrderRequest(

        @Size(max = 255, message = "취소 사유는 255자 이하여야 합니다.")
        String cancelReason  // 취소 사유 (선택)
) {}