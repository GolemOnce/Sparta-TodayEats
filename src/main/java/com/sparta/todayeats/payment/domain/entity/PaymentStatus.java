package com.sparta.todayeats.payment.domain.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum PaymentStatus {
    PENDING, COMPLETED, CANCELLED
}
