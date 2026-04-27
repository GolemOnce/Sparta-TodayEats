package com.sparta.todayeats.payment.dto.response;

import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.payment.entity.Payment;
import com.sparta.todayeats.payment.entity.PaymentMethod;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentDetailResponse {
    private UUID paymentId;
    private Long amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    private OrderSummary order;  // 중첩 객체

    @Getter
    @Builder
    public static class OrderSummary {
        private UUID orderId;
        private OrderStatus status;
        private String storeName;
        private LocalDateTime createdAt;
        private UUID createdBy;
        private LocalDateTime updatedAt;
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
                        .orderId(payment.getOrder().getId())
                        .status(payment.getOrder().getStatus())
                        .storeName(payment.getOrder().getStoreName())
                        .createdBy(payment.getOrder().getCreatedBy())
                        .updatedAt(payment.getOrder().getUpdatedAt())
                        .updatedBy(payment.getOrder().getUpdatedBy())
                        .build())
                .build();
    }
}