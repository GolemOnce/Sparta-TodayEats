package com.sparta.todayeats.review.controller;

import com.sparta.todayeats.global.annotation.ApiNoContent;
import com.sparta.todayeats.global.annotation.ApiPageable;
import com.sparta.todayeats.global.annotation.LoginUser;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.review.dto.request.ReviewCreateRequest;
import com.sparta.todayeats.review.dto.request.ReviewUpdateRequest;
import com.sparta.todayeats.review.dto.response.*;
import com.sparta.todayeats.review.service.ReviewService;
import com.sparta.todayeats.user.entity.UserRoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Review")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // 리뷰 등록 (POST /orders/{orderId}/reviews) 본인
    @Operation(summary = "리뷰 작성")
    @PostMapping("/orders/{orderId}/reviews")
    public ResponseEntity<ApiResponse<ReviewCreateResponse>> createReview(
            @Parameter(description = "주문 ID", example = "770e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID orderId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            @RequestBody ReviewCreateRequest request) {

        ReviewCreateResponse response = reviewService.createReview(orderId, userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    // 내 리뷰 조회 (GET /reviews) CUSTOMER + 특정 유저 리뷰 조회 (GET /reviews?userId=UUID) ADMIN
    @Operation(summary = "사용자 리뷰 목록 조회")
    @ApiPageable
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviews(
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Parameter(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(name = "userId", required = false) UUID targetId,
            @Parameter(hidden = true) Pageable pageable,
            Authentication authentication
    ) {
        UserRoleEnum role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> UserRoleEnum.valueOf(a.getAuthority()))
                .orElseThrow();

        UUID targetUserId = targetId != null ? targetId : userId;

        PageResponse<ReviewResponse> response = reviewService.getPagedReviews(userId, targetUserId, role, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 특정 가게 리뷰 조회 (GET /stores/{storeId}/reviews 모두
    @Operation(summary = "가게 리뷰 목록 조회")
    @ApiPageable
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getStoreReviews(
            @Parameter(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId,
            @Parameter(hidden = true) Pageable pageable
    ) {

        PageResponse<ReviewResponse> response = reviewService.getStoreReviews(storeId, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 리뷰 상세 조회 (GET /reviews/{reviewId}) 모두
    @Operation(summary = "리뷰 상세 조회")
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> getReview(
            @Parameter(description = "리뷰 ID", example = "r10e8400-e29b-41d4-a716-446655447777")
            @PathVariable UUID reviewId
    ) {
        ReviewDetailResponse response = reviewService.getDetailReview(reviewId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 리뷰 수정 (PUT /reviews/{reviewId}) 본인
    @Operation(summary = "리뷰 수정")
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewUpdateResponse>> updateReview(
            @Parameter(description = "리뷰 ID", example = "r10e8400-e29b-41d4-a716-446655447777")
            @PathVariable UUID reviewId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            @RequestBody ReviewUpdateRequest request
    ) {
        ReviewUpdateResponse response = reviewService.updateReview(reviewId, userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 리뷰 삭제 (DELETE /reviews/{reviewId}) 본인 + 어드민
    @Operation(summary = "리뷰 삭제")
    @ApiNoContent
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "리뷰 ID", example = "r10e8400-e29b-41d4-a716-446655447777")
            @PathVariable UUID reviewId,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}
