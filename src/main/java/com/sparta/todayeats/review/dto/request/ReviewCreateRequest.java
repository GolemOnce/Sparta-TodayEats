package com.sparta.todayeats.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Schema(description = "리뷰 작성 요청")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreateRequest {
    @Schema(description = "별점 (1~5점)", example = "5")
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Schema(description = "내용", example = "만두 피가 얇고 속이 꽉 차서 정말 맛있어요!")
    @NotBlank
    private String Content;
}
