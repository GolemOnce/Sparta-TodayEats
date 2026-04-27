package com.sparta.todayeats.review.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReviewCreateResponse {
    private UUID reviewId;
    private UUID orderId;
    private UUID storeId;
    private UUID customerId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private UUID createdBy;
}