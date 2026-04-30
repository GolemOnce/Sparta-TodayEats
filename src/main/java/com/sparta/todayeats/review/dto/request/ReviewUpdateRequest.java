package com.sparta.todayeats.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "리뷰 수정 요청")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewUpdateRequest {
    @Schema(description = "별점 (1~5점)", example = "5")
    private Integer rating;

    @Schema(description = "내용", example = "만두 피가 얇고 속이 꽉 차서 정말 맛있어요!")
    private String content;
}
