package com.sparta.todayeats.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreateRequest {
    @NotNull
    private Integer rating;

    @NotBlank
    private String Content;
}
