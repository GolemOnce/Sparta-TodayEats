package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MenuErrorCode implements ErrorCode {
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU-001", "메뉴를 찾을 수 없습니다."),
    MENU_NOT_IN_STORE(HttpStatus.BAD_REQUEST, "MENU-002", "해당 가게의 메뉴가 아닙니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}