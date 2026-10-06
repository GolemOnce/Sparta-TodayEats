package com.sparta.todayeats.payment.dto.response;

import com.sparta.todayeats.payment.entity.Payment;
import com.sparta.todayeats.payment.entity.PaymentMethod;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "결제 정보 응답")
@Getter
@Builder
public class PaymentResponse {
    @Schema(description = "결제 ID", example = "p10e8400-e29b-41d4-a716-446655449999")
    private UUID paymentId;

    @Schema(description = "주문 ID", example = "770e8400-e29b-41d4-a716-446655440000")
    private UUID orderId;

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

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .method(payment.getPaymentMethod())
                .createdAt(payment.getCreatedAt())
                .createdBy(payment.getCreatedBy())
                .updatedAt(payment.getUpdatedAt())
                .updatedBy(payment.getUpdatedBy())
                .build();
    }
}
