package com.sparta.todayeats.order.dto.response;

import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "주문 취소 응답")
public record CancelOrderResponse(
        @Schema(description = "주문 ID", example = "770e8400-e29b-41d4-a716-446655440000")
        UUID orderId,

        @Schema(description = "주문 상태", example = "CANCELED")
        OrderStatus status,

        @Schema(description = "취소자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
        UUID canceledBy,

        @Schema(description = "취소 시간", example = "2026-04-30T15:10:00")
        LocalDateTime canceledAt
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