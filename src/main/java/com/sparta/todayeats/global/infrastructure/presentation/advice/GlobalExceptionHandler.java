package com.sparta.todayeats.global.infrastructure.presentation.advice;

import com.sparta.todayeats.global.exception.AuthErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.CommonErrorCode;
import com.sparta.todayeats.global.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException e) {
        return buildResponse(e.getErrorCode());
    }

    // 접근 권한 없음 (403)
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AuthorizationDeniedException e) {
        return buildResponse(AuthErrorCode.FORBIDDEN);
    }

    // 지원하지 않는 HTTP 메서드 요청 (405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return buildResponse(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    // 경로를 찾을 수 없음 (404)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException e) {
        return buildResponse(CommonErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        e.printStackTrace();
        return buildResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    // @Valid 검증 실패 시 발생하는 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now(),
                "code", "VALID-001",
                "message", message
        ));
    }

    private ResponseEntity<?> buildResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "code", errorCode.getCode(),
                        "message", errorCode.getMessage()
                ));
    }
}