package com.sparta.todayeats.order.dto.response;

import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderItem;
import com.sparta.todayeats.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "주문 생성 응답")
public record CreateOrderResponse(
        @Schema(description = "주문 ID", example = "770e8400-e29b-41d4-a716-446655440000")
        UUID orderId,

        @Schema(description = "주문 상태", example = "PENDING")
        OrderStatus status, // 최초 생성 시 PENDING

        StoreInfo store,    // 가게 정보 (스냅샷)
        DeliveryInfo delivery,  // 배송지 정보 (스냅샷)
        List<OrderItemInfo> items,  // 주문 메뉴 목록 (스냅샷)

        @Schema(description = "주문 요청사항", example = "단무지 많이 주세요!")
        String note,

        @Schema(description = "총 주문 금액", example = "11000")
        Long totalPrice,    // 서버에서 계산

        @Schema(description = "생성 시간", example = "2026-04-30T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
        UUID createdBy
) {
    /**
     * OrderEntity로부터 응답 DTO 생성
     */
    public static CreateOrderResponse from(Order order) {
        return new CreateOrderResponse(
                order.getOrderId(),
                order.getStatus(),
                new StoreInfo(order.getStoreId(), order.getStoreName()),
                new DeliveryInfo(order.getAddressId(), order.getDeliveryAddress(), order.getDeliveryDetail()),
                order.getOrderItems().stream().map(OrderItemInfo::from).toList(),
                order.getNote(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getCreatedBy()
        );
    }

    @Schema(description = "주문 시점 가게 정보 스냅샷")
    public record StoreInfo(
            @Schema(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            UUID storeId,

            @Schema(description = "가게 이름", example = "맛있는 한식당")
            String storeName
    ) {}

    @Schema(description = "주문 시점 배송지 정보 스냅샷")
    public record DeliveryInfo(
            @Schema(description = "배송지 ID", example = "dd0e8400-e29b-41d4-a716-446655442222")
            UUID addressId,

            @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 311")
            String address,

            @Schema(description = "상세 주소", example = "3층")
            String detail
    ) {}

    @Schema(description = "주문 시점 메뉴 정보 스냅샷")
    public record OrderItemInfo(
            @Schema(description = "메뉴 ID", example = "m10e8400-e29b-41d4-a716-446655443333")
            UUID menuId,

            @Schema(description = "메뉴 이름", example = "고기듬뿍 고향만두")
            String menuName,

            @Schema(description = "주문 수량", example = "2")
            Integer quantity,

            @Schema(description = "메뉴 단가", example = "5500")
            Long unitPrice
    ) {
        public static OrderItemInfo from(OrderItem item) {
            return new OrderItemInfo(
                    item.getMenuId(),
                    item.getMenuName(),
                    item.getQuantity(),
                    item.getUnitPrice()
            );
        }
    }
}