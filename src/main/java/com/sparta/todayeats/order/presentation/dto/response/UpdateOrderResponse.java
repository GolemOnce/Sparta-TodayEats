package com.sparta.todayeats.order.presentation.dto.response;

import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateOrderResponse(
        UUID orderId,            // 주문 고유 ID
        OrderStatus status,      // 주문 상태
        String note,             // 수정된 요청사항
        LocalDateTime updatedAt, // 수정 일시 (JPA Auditing)
        UUID updatedBy           // 수정자 (JPA Auditing)
) {
    public static UpdateOrderResponse from(OrderEntity order) {
        return new UpdateOrderResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getNote(),
                order.getUpdatedAt(),
                order.getUpdatedBy()
        );
    }
}