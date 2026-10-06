package com.sparta.todayeats.order.dto.request;

import com.sparta.todayeats.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 상태 변경 요청")
public record UpdateOrderStatusRequest(
        @Schema(description = "주문 상태", example = "ACCEPTED")
        @NotNull(message = "status는 필수입니다.")
        OrderStatus status
) {}