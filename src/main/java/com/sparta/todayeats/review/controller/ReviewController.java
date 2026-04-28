package com.sparta.todayeats.review.controller;

import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.review.dto.request.ReviewCreateRequest;
import com.sparta.todayeats.review.dto.request.ReviewUpdateRequest;
import com.sparta.todayeats.review.dto.response.ReviewCreateResponse;
import com.sparta.todayeats.review.dto.response.ReviewDetailResponse;
import com.sparta.todayeats.review.dto.response.ReviewPageResponse;
import com.sparta.todayeats.review.dto.response.ReviewUpdateResponse;
import com.sparta.todayeats.review.service.ReviewService;
import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    // 내 리뷰 조회 (GET /reviews) CUSTOMER + 특정 유저 리뷰 조회 (GET /reviews?userId=UUID) ADMIN
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewPageResponse>> getReviews(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) UUID targetId,
            Pageable pageable,
            Authentication authentication
    ) {
        UserRoleEnum role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> UserRoleEnum.valueOf(a.getAuthority()))
                .orElseThrow();

        UUID targetUserId = targetId != null ? targetId : userId;

        ReviewPageResponse response = reviewService.getPagedReviews(userId, targetUserId, role, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 특정 가게 리뷰 조회 (GET /stores/{storeId}/reviews 모두
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<ApiResponse<ReviewPageResponse>> getStoreReviews(
            @PathVariable UUID storeId,
            Pageable pageable) {

        ReviewPageResponse response = reviewService.getStoreReviews(storeId, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 리뷰 상세 조회 (GET /reviews/{reviewId}) 모두
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> getReview(
            @PathVariable("reviewId") UUID reviewId
    ) {
        ReviewDetailResponse response = reviewService.getDetailReview(reviewId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 리뷰 수정 (PUT /reviews/{reviewId}) 본인
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewUpdateResponse>> updateReview(
            @PathVariable("reviewId") UUID reviewId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody ReviewUpdateRequest request
    ) {
        ReviewUpdateResponse response = reviewService.updateReview(reviewId, userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 리뷰 삭제 (DELETE /reviews/{reviewId}) 본인 + 어드민
}
