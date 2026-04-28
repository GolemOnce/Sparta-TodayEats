package com.sparta.todayeats.order.dto.response;

import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/** 주문 수정 응답 DTO */
public record UpdateOrderResponse(
        UUID orderId,               // 주문 고유 ID
        OrderStatus status,         // 주문 상태
        String note,                // 수정된 요청사항
        LocalDateTime createdAt,    // 생성 일시 (JPA Auditing)
        UUID createdBy,             // 생성자 (JPA Auditing)
        LocalDateTime updatedAt,    // 수정 일시 (JPA Auditing)
        UUID updatedBy              // 수정자 (JPA Auditing)
) {
    /**
     * OrderEntity로부터 응답 DTO 생성
     */
    public static UpdateOrderResponse from(Order order) {
        return new UpdateOrderResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getNote(),
                order.getCreatedAt(),
                order.getCreatedBy(),
                order.getUpdatedAt(),
                order.getUpdatedBy()
        );
    }
}