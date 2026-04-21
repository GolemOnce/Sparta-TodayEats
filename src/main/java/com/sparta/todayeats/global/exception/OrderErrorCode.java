package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-001", "주문을 찾을 수 없습니다."),
    CANCEL_TIME_EXCEEDED(HttpStatus.BAD_REQUEST, "ORDER-002", "취소 가능 시간이 지났습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}