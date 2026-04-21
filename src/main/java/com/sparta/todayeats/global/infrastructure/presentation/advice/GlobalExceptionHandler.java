package com.sparta.todayeats.global.infrastructure.presentation.advice;

import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "code", errorCode.getCode(),
                        "message", errorCode.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "code", "INTERNAL_SERVER_ERROR",
                        "message", "서버 내부 오류가 발생했습니다."
                ));
    }
}