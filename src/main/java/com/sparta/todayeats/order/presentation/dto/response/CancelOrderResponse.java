package com.sparta.todayeats.order.presentation.dto.response;

import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CancelOrderResponse(
        UUID orderId,               // 주문 고유 ID
        OrderStatus status,         // 주문 상태 (CANCELED)
        UUID canceledBy,            // 취소한 사용자 ID (updatedBy)
        LocalDateTime canceledAt    // 취소 일시 (updatedAt)
) {
    public static CancelOrderResponse from(OrderEntity order) {
        return new CancelOrderResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getUpdatedBy(),   // JPA Auditing 자동 세팅
                order.getUpdatedAt()    // JPA Auditing 자동 세팅
        );
    }
}