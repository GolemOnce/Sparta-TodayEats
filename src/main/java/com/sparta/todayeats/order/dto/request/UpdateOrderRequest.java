package com.sparta.todayeats.order.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 주문 요청사항 수정 DTO
 */
public record UpdateOrderRequest(

        @Size(max = 255, message = "요청사항은 255자 이하여야 합니다.")
        String note  // 요청사항 수정 (선택)
) {}