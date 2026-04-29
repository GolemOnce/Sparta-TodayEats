package com.sparta.todayeats.review.dto.response;

import com.sparta.todayeats.review.entity.Review;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ReviewPageResponse {
    private List<ReviewResponse> reviews;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;

    public static ReviewPageResponse from(Page<Review> reviewPage) {
        return ReviewPageResponse.builder()
                .reviews(reviewPage.getContent().stream()
                        .map(ReviewResponse::from)
                        .toList())
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .sort(reviewPage.getSort().isSorted() ? reviewPage.getSort().toString() : "createdAt,DESC")
                .build();
    }
}
