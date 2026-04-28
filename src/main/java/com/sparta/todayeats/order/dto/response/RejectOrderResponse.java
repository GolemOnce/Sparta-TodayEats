package com.sparta.todayeats.order.dto.response;

import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/** 주문 거절 응답 DTO */
public record RejectOrderResponse(
        UUID orderId,               // 주문 고유 ID
        OrderStatus status,         // 주문 상태 (REJECTED)
        String rejectReason,        // 거절 사유
        LocalDateTime updatedAt,    // 수정 일시 (JPA Auditing)
        UUID updatedBy              // 수정자 (JPA Auditing)
) {
    /**
     * OrderEntity로부터 응답 DTO 생성
     */
    public static RejectOrderResponse from(Order order) {
        return new RejectOrderResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getRejectReason(),
                order.getUpdatedAt(),
                order.getUpdatedBy()
        );
    }
}