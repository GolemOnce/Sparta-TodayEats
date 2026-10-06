package com.sparta.todayeats.payment.dto.response;

import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.payment.entity.Payment;
import com.sparta.todayeats.payment.entity.PaymentMethod;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "결제 상세 정보 응답")
@Getter
@Builder
public class PaymentDetailResponse {
    @Schema(description = "결제 ID", example = "p10e8400-e29b-41d4-a716-446655449999")
    private UUID paymentId;

    @Schema(description = "결제 금액", example = "11000")
    private Long amount;

    @Schema(description = "결제 수단", example = "CARD")
    private PaymentMethod method;

    @Schema(description = "결제 상태", example = "COMPLETED")
    private PaymentStatus status;

    @Schema(description = "생성 시간", example = "2026-04-30T15:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
    private UUID createdBy;

    @Schema(description = "수정 시간", example = "2026-04-30T15:05:00")
    private LocalDateTime updatedAt;

    @Schema(description = "수정자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
    private UUID updatedBy;

    private OrderSummary order;  // 중첩 객체

    @Schema(description = "주문 정보 응답")
    @Getter
    @Builder
    public static class OrderSummary {
        @Schema(description = "주문 ID", example = "770e8400-e29b-41d4-a716-446655440000")
        private UUID orderId;

        @Schema(description = "주문 상태", example = "SHIPPING")
        private OrderStatus status;

        @Schema(description = "가게 이름 스냅샷", example = "맛있는 한식당")
        private String storeName;

        @Schema(description = "생성 시간", example = "2026-04-30T15:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
        private UUID createdBy;

        @Schema(description = "수정 시간", example = "2026-04-30T15:20:00")
        private LocalDateTime updatedAt;

        @Schema(description = "수정자 ID", example = "990e8400-e29b-41d4-a716-446655449999")
        private UUID updatedBy;
    }

    public static PaymentDetailResponse from(Payment payment) {
        return PaymentDetailResponse.builder()
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .method(payment.getPaymentMethod())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .createdBy(payment.getCreatedBy())
                .updatedAt(payment.getUpdatedAt())
                .updatedBy(payment.getUpdatedBy())
                .order(OrderSummary.builder()
                        .orderId(payment.getOrder().getOrderId())
                        .status(payment.getOrder().getStatus())
                        .storeName(payment.getOrder().getStoreName())
                        .createdAt(payment.getOrder().getCreatedAt())
                        .createdBy(payment.getOrder().getCreatedBy())
                        .updatedAt(payment.getOrder().getUpdatedAt())
                        .updatedBy(payment.getOrder().getUpdatedBy())
                        .build())
                .build();
    }
}