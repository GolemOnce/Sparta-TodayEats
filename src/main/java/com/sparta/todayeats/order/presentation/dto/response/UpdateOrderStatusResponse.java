package com.sparta.todayeats.order.presentation.dto.response;

import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateOrderStatusResponse(
        UUID orderId,               // 주문 고유 ID
        OrderStatus status,         // 변경된 주문 상태
        LocalDateTime createdAt,    // 생성 일시 (JPA Auditing)
        UUID createdBy,             // 생성자 (JPA Auditing)
        LocalDateTime updatedAt,    // 수정 일시 (JPA Auditing)
        UUID updatedBy              // 수정자 (JPA Auditing)
) {
    /**
     * OrderEntity로부터 응답 DTO 생성
     */
    public static UpdateOrderStatusResponse from(OrderEntity order) {
        return new UpdateOrderStatusResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getCreatedBy(),
                order.getUpdatedAt(),
                order.getUpdatedBy()
        );
    }
}