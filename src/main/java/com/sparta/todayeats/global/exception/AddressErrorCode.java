package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AddressErrorCode implements ErrorCode {
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDR-001", "배송지를 찾을 수 없습니다."),
    ADDRESS_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ADDR-002", "본인 배송지만 접근할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}