package com.sparta.todayeats.order.presentation.dto.request;

import com.sparta.todayeats.order.domain.entity.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(

        @NotNull(message = "storeId는 필수입니다.")
        UUID storeId,

        @NotNull(message = "addressId는 필수입니다.")
        UUID addressId,

        @NotNull(message = "orderType은 필수입니다.")
        OrderType orderType,

        String note,

        @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
        @Valid
        @NotNull
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(

            @NotNull(message = "menuId는 필수입니다.")
            UUID menuId,

            @NotNull(message = "quantity는 필수입니다.")
            @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
            Integer quantity
    ) {}
}