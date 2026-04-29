package com.sparta.todayeats.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiProductDescriptionRequest(
        @NotBlank(message = "궁금한 점을 입력해주세요.")
        @Size(max = 100, message = "최대 100자까지 입력할 수 있습니다.")
        String prompt
) {
}
