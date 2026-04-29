package com.sparta.todayeats.payment.dto.response;

import com.sparta.todayeats.payment.entity.PaymentMethod;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentCreateResponse {
    private UUID paymentId;
    private UUID orderId;
    private Long amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private UUID createdBy;
}
