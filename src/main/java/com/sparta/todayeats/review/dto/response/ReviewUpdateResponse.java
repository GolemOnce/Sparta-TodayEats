package com.sparta.todayeats.review.dto.response;

import com.sparta.todayeats.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReviewUpdateResponse {
    private UUID reviewId;
    private UUID orderId;
    private UUID storeId;
    private UUID customerId;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    public static ReviewUpdateResponse from(Review review) {
        return ReviewUpdateResponse.builder()
                .reviewId(review.getId())
                .orderId(review.getOrder().getOrderId())
                .storeId(review.getStore().getId())
                .customerId(review.getUser().getUserId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .createdBy(review.getCreatedBy())
                .updatedAt(review.getUpdatedAt())
                .updatedBy(review.getUpdatedBy())
                .build();
    }
}
