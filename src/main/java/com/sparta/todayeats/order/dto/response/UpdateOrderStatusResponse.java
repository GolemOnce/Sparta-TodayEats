package com.sparta.todayeats.order.dto.response;

import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "주문 상태 변경 응답")
public record UpdateOrderStatusResponse(
        @Schema(description = "주문 ID", example = "770e8400-e29b-41d4-a716-446655440000")
        UUID orderId,

        @Schema(description = "주문 상태", example = "ACCEPTED")
        OrderStatus status,

        @Schema(description = "생성 시간", example = "2026-04-30T15:00:00")
        LocalDateTime createdAt,

        @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
        UUID createdBy,

        @Schema(description = "수정 시간", example = "2026-04-30T15:05:00")
        LocalDateTime updatedAt,

        @Schema(description = "수정자 ID", example = "990e8400-e29b-41d4-a716-446655449999")
        UUID updatedBy
) {
    /**
     * OrderEntity로부터 응답 DTO 생성
     */
    public static UpdateOrderStatusResponse from(Order order) {
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