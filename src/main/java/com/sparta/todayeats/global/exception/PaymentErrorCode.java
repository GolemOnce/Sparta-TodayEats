package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PAY-001", "결제에 실패했습니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PAY-002", "이미 결제된 주문입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}