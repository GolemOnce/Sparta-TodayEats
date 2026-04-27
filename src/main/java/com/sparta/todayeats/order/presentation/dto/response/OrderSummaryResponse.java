package com.sparta.todayeats.order.presentation.dto.response;

import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/** 주문 목록 응답 DTO */
public record OrderSummaryResponse(
        UUID orderId,           // 주문 고유 ID
        OrderStatus status,     // 주문 상태
        String storeName,       // 가게명 (스냅샷)
        Long totalPrice,        // 총 주문 금액
        LocalDateTime createdAt, // 주문 생성 일시 (JPA Auditing)
        UUID createdBy,         // 주문자 ID (JPA Auditing)
        LocalDateTime updatedAt, // 수정 일시 (JPA Auditing)
        UUID updatedBy          // 수정자 ID (JPA Auditing)
) {
    /**
     * OrderEntity로부터 응답 DTO 생성
     */
    public static OrderSummaryResponse from(OrderEntity order) {
        return new OrderSummaryResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getStoreName(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getCreatedBy(),
                order.getUpdatedAt(),
                order.getUpdatedBy()
        );
    }
}