package com.sparta.todayeats.order.dto.response;

import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "주문 거절 응답")
public record RejectOrderResponse(
        @Schema(description = "주문 ID", example = "770e8400-e29b-41d4-a716-446655440000")
        UUID orderId,

        @Schema(description = "주문 상태", example = "REJECTED")
        OrderStatus status,

        @Schema(description = "거절 사유", example = "재료가 모두 소진되었습니다.")
        String rejectReason,

        @Schema(description = "수정 시간", example = "2026-04-30T15:05:00")
        LocalDateTime updatedAt,

        @Schema(description = "수정자 ID", example = "990e8400-e29b-41d4-a716-446655449999")
        UUID updatedBy
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