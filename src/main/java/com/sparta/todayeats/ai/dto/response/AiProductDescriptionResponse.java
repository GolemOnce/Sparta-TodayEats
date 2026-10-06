package com.sparta.todayeats.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 상품 설명 생성 응답")
public record AiProductDescriptionResponse(
        @Schema(description = "AI 프롬프트", example = "만두 상품의 이름을 추천해줘")
        String prompt,
        @Schema(description = "AI 생성 결과", example = "왕만두, 명품수제만두, 한입만두")
        String result
) {
}
