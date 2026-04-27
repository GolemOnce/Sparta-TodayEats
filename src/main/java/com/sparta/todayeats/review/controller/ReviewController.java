package com.sparta.todayeats.review.controller;

import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.review.dto.request.ReviewCreateRequest;
import com.sparta.todayeats.review.dto.response.ReviewCreateResponse;
import com.sparta.todayeats.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // 리뷰 등록 (POST /orders/{orderId}/reviews) 본인
    @PostMapping("/orders/{orderId}/reviews")
    public ResponseEntity<ApiResponse<ReviewCreateResponse>> createReview(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody ReviewCreateRequest request) {

        ReviewCreateResponse response = reviewService.createReview(orderId, userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // 리뷰 조회 (GET /reviews) 어드민 - 가게/사용자 리뷰 조회, 고객-고객리뷰, 가게-가게리뷰

    // 리뷰 상세 조회 (GET /reviews/{reviewId}) 모두

    // 리뷰 수정 (PUT /reviews/{reviewId}) 본인

    // 리뷰 삭제 (DELETE /reviews/{reviewId}) 본인+어드민
}
