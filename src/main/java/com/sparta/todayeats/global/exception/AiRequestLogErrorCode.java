package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AiRequestLogErrorCode implements ErrorCode {
    LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "AI-001", "로그를 찾을 수 없습니다."),
    AI_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "AI-002", "Gemini 응답이 비어있습니다."),
    AI_API_ERROR(HttpStatus.BAD_GATEWAY, "AI-003", "Gemini API 호출에 실패했습니다."),
    AI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI-004", "Gemini API 응답 시간이 초과되었습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}