package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategoryErrorCode implements ErrorCode {
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CAT-001", "카테고리를 찾을 수 없습니다."),
    CATEGORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "CAT-002", "이미 존재하는 카테고리입니다."),
    INVALID_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "CAT-003", "유효하지 않은 카테고리 이름입니다."),
    CATEGORY_HAS_STORES(HttpStatus.BAD_REQUEST, "CAT-004", "해당 카테고리에 등록된 가게가 있어 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}