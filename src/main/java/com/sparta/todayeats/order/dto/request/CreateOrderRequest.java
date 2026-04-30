package com.sparta.todayeats.order.dto.request;

import com.sparta.todayeats.order.entity.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "주문 생성 요청")
public record CreateOrderRequest(
        @Schema(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "storeId는 필수입니다.")
        UUID storeId,

        @Schema(description = "배송지 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "addressId는 필수입니다.")
        UUID addressId,

        @Schema(description = "주문 유형", example = "DELIVERY")
        @NotNull(message = "orderType은 필수입니다.")
        OrderType orderType,

        @Schema(description = "주문 요청사항", example = "단무지 많이 주세요!")
        String note,

        @Schema(description = "주문 상품 목록")
        @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
        @Valid
        List<@NotNull OrderItemRequest> items
) {
    @Schema(description = "주문 상품 상세 요청")
    public record OrderItemRequest(
            @Schema(description = "메뉴 ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
            @NotNull(message = "menuId는 필수입니다.")
            UUID menuId,

            @Schema(description = "주문 수량", example = "2")
            @NotNull(message = "quantity는 필수입니다.")
            @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
            Integer quantity
    ) {}
}