package com.sparta.todayeats.order.presentation.dto.response;

import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderItemEntity;
import com.sparta.todayeats.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId,           // 주문 고유 ID
        OrderStatus status,     // 주문 상태 (최초 생성 시 PENDING)
        StoreInfo store,        // 가게 정보 (스냅샷)
        DeliveryInfo delivery,  // 배송지 정보 (스냅샷)
        List<OrderItemInfo> items, // 주문 메뉴 목록 (스냅샷)
        String note,            // 주문 요청사항
        Long totalPrice,        // 총 주문 금액 (서버에서 계산)
        LocalDateTime createdAt, // 주문 생성 일시 (JPA Auditing)
        UUID createdBy          // 주문자 ID (JPA Auditing)
) {
    /**
     * OrderEntity로부터 응답 DTO 생성
     */
    public static CreateOrderResponse from(OrderEntity order) {
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

    // 가게 정보 스냅샷 (주문 시점 가게명 고정)
    public record StoreInfo(
            UUID storeId,       // 가게 고유 ID
            String storeName    // 가게명 스냅샷
    ) {}

    // 배송지 정보 스냅샷 (주문 시점 배송지 고정)
    public record DeliveryInfo(
            UUID addressId,     // 배송지 고유 ID
            String address,     // 도로명 주소 스냅샷
            String detail       // 상세주소 스냅샷
    ) {}

    // 주문 메뉴 정보 스냅샷 (주문 시점 메뉴명/단가 고정)
    public record OrderItemInfo(
            UUID menuId,        // 메뉴 고유 ID
            String menuName,    // 메뉴명 스냅샷
            Integer quantity,   // 주문 수량
            Long unitPrice   // 단가 스냅샷
    ) {
        public static OrderItemInfo from(OrderItemEntity item) {
            return new OrderItemInfo(
                    item.getMenuId(),
                    item.getMenuName(),
                    item.getQuantity(),
                    item.getUnitPrice()
            );
        }
    }
}