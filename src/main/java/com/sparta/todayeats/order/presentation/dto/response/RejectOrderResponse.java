package com.sparta.todayeats.order.presentation.dto.response;

import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RejectOrderResponse(
        UUID orderId,               // 주문 고유 ID
        OrderStatus status,         // 주문 상태 (REJECTED)
        String rejectReason,        // 거절 사유
        LocalDateTime updatedAt,    // 수정 일시 (JPA Auditing)
        UUID updatedBy              // 수정자 (JPA Auditing)
) {
    public static RejectOrderResponse from(OrderEntity order) {
        return new RejectOrderResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getRejectReason(),
                order.getUpdatedAt(),
                order.getUpdatedBy()
        );
    }
}