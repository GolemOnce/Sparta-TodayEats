package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AiRequestLogErrorCode implements ErrorCode {
    LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "AI-001", "로그를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}