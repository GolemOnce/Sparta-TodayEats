package com.sparta.todayeats.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode implements ErrorCode {
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW-001", "리뷰를 찾을 수 없습니다."),
    REVIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "REVIEW-002", "리뷰 조회 권한이 없습니다."),
    REVIEW_INVALID_RATING(HttpStatus.BAD_REQUEST, "REVIEW-003", "평점은 1~5 사이여야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}