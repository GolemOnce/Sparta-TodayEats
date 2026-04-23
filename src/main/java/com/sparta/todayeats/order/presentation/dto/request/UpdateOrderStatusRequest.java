package com.sparta.todayeats.order.presentation.dto.request;

import com.sparta.todayeats.order.domain.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "status는 필수입니다.")
        OrderStatus status  // 변경할 주문 상태
) {}