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
    DUPLICATE_PASSWORD(HttpStatus.BAD_REQUEST, "USER-004", "기존과 동일한 비밀번호는 사용할 수 없습니다."),
    CANNOT_UPDATE_MASTER_ROLE(HttpStatus.BAD_REQUEST, "USER-005", "최상위 관리자의 권한은 변경할 수 없습니다."),
    CANNOT_GRANT_MASTER_ROLE(HttpStatus.BAD_REQUEST, "USER-006", "최상위 관리자 권한은 부여할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}