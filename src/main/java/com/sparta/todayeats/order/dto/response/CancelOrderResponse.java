package com.sparta.todayeats.order.dto.response;

import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/** 주문 취소 응답 DTO */
public record CancelOrderResponse(
        UUID orderId,               // 주문 고유 ID
        OrderStatus status,         // 주문 상태 (CANCELED)
        UUID canceledBy,            // 취소한 사용자 ID (updatedBy)
        LocalDateTime canceledAt    // 취소 일시 (updatedAt)
) {
    /**
     * OrderEntity로부터 응답 DTO 생성
     */
    public static CancelOrderResponse from(Order order) {
        return new CancelOrderResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getUpdatedBy(),   // JPA Auditing 자동 세팅
                order.getUpdatedAt()    // JPA Auditing 자동 세팅
        );
    }
}