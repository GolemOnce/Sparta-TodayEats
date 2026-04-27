package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements ErrorCode {
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE-001", "가게를 찾을 수 없습니다."),
    STORE_ALREADY_EXISTS(HttpStatus.CONFLICT, "STORE-002", "이미 존재하는 가게 이름입니다"),
    STORE_FORBIDDEN(HttpStatus.FORBIDDEN, "STORE-003", "해당 가게에 대한 접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}