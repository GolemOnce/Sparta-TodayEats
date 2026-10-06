package com.sparta.todayeats.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "주문 요청사항 수정")
public record UpdateOrderRequest(
        @Schema(description = "주문 요청사항", example = "벨 누르지 말고 문 앞에 놔주세요.")
        @Size(max = 255, message = "요청사항은 255자 이하여야 합니다.")
        String note
) {}