package com.sparta.todayeats.order.dto.response;

import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "주문 정보 응답")
public record OrderSummaryResponse(
        @Schema(description = "주문 ID", example = "770e8400-e29b-41d4-a716-446655440000")
        UUID orderId,

        @Schema(description = "주문 상태", example = "SHIPPING")
        OrderStatus status,

        @Schema(description = "가게 이름 스냅샷", example = "맛있는 한식당")
        String storeName,

        @Schema(description = "총 주문 금액", example = "11000")
        Long totalPrice,

        @Schema(description = "주문 시간", example = "2026-04-30T15:00:00")
        LocalDateTime createdAt,

        @Schema(description = "주문자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
        UUID createdBy,

        @Schema(description = "수정 시간", example = "2026-04-30T15:20:00")
        LocalDateTime updatedAt,

        @Schema(description = "수정자 ID", example = "990e8400-e29b-41d4-a716-446655449999")
        UUID updatedBy
) {
    /**
     * OrderEntity로부터 응답 DTO 생성
     */
    public static OrderSummaryResponse from(Order order) {
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