package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER-002", "이미 사용 중인 이메일입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "USER-003", "비밀번호가 일치하지 않습니다."),
    DUPLICATE_PASSWORD(HttpStatus.BAD_REQUEST, "USER-004", "기존과 동일한 비밀번호는 사용할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}