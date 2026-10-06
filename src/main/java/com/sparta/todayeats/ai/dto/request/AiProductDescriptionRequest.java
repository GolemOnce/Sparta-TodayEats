package com.sparta.todayeats.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 상품 설명 생성 요청")
public record AiProductDescriptionRequest(
        @Schema(description = "AI 프롬프트", example = "만두 상품의 이름을 추천해줘")
        @NotBlank(message = "궁금한 점을 입력해주세요.")
        @Size(max = 100, message = "최대 100자까지 입력할 수 있습니다.")
        String prompt
) {
}
