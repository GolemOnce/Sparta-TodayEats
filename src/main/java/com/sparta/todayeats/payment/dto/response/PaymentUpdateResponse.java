package com.sparta.todayeats.payment.dto.response;

import com.sparta.todayeats.payment.entity.Payment;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentUpdateResponse {
    private UUID paymentId;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    public static PaymentUpdateResponse from(Payment payment) {
        return PaymentUpdateResponse.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .createdBy(payment.getCreatedBy())
                .updatedAt(payment.getUpdatedAt())
                .updatedBy(payment.getUpdatedBy())
                .build();
    }
}
