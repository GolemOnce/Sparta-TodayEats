package com.sparta.todayeats.review.dto.response;

import com.sparta.todayeats.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "리뷰 정보 응답")
@Getter
@Builder
public class ReviewResponse {
    @Schema(description = "리뷰 ID", example = "r10e8400-e29b-41d4-a716-446655447777")
    private UUID reviewId;

    @Schema(description = "주문 ID", example = "770e8400-e29b-41d4-a716-446655440000")
    private UUID orderId;

    @Schema(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID storeId;

    @Schema(description = "고객 ID", example = "550e8400-e29b-41d4-a716-446655441111")
    private UUID customerId;

    @Schema(description = "별점", example = "5")
    private Integer rating;

    @Schema(description = "내용", example = "만두 피가 얇고 속이 꽉 차서 정말 맛있어요!")
    private String content;

    @Schema(description = "생성 시간", example = "2026-04-30T18:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
    private UUID createdBy;

    @Schema(description = "수정 시간", example = "2026-04-30T19:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "수정자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
    private UUID updatedBy;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
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
