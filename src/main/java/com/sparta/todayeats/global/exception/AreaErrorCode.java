package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AreaErrorCode implements ErrorCode {
    AREA_NOT_FOUND(HttpStatus.NOT_FOUND, "AREA-001", "지역을 찾을 수 없습니다."),
    AREA_ALREADY_EXISTS(HttpStatus.CONFLICT, "AREA-002", "이미 존재하는 지역입니다."),
    INVALID_AREA_NAME(HttpStatus.BAD_REQUEST, "AREA-003", "유효하지 않은 지역 이름입니다."),
    AREA_INACTIVE(HttpStatus.BAD_REQUEST, "AREA-004", "비활성화된 지역입니다."),
    AREA_HAS_STORES(HttpStatus.BAD_REQUEST, "AREA-005", "해당 운영지역에 등록된 가게가 있어 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}