package com.sparta.todayeats.order.presentation.dto.response;

import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderItemEntity;
import com.sparta.todayeats.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID orderId,               // 주문 고유 ID
        OrderStatus status,         // 주문 상태
        StoreInfo store,            // 가게 정보 (스냅샷)
        DeliveryInfo delivery,      // 배송지 정보 (스냅샷)
        List<OrderItemInfo> items,  // 주문 메뉴 목록 (스냅샷)
        String note,                // 주문 요청사항
        Long totalPrice,            // 총 주문 금액
        LocalDateTime createdAt,    // 주문 생성 일시 (JPA Auditing)
        UUID createdBy,             // 주문자 ID (JPA Auditing)
        LocalDateTime updatedAt,    // 수정 일시 (JPA Auditing)
        UUID updatedBy              // 수정자 ID (JPA Auditing)
) {
    public static OrderDetailResponse from(OrderEntity order) {
        return new OrderDetailResponse(
                order.getOrderId(),
                order.getStatus(),
                new StoreInfo(order.getStoreId(), order.getStoreName()),
                new DeliveryInfo(order.getAddressId(), order.getDeliveryAddress(), order.getDeliveryDetail()),
                order.getOrderItems().stream().map(OrderItemInfo::from).toList(),
                order.getNote(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getCreatedBy(),
                order.getUpdatedAt(),
                order.getUpdatedBy()
        );
    }

    // 가게 정보 스냅샷
    public record StoreInfo(
            UUID storeId,       // 가게 고유 ID
            String storeName    // 가게명 스냅샷
    ) {}

    // 배송지 정보 스냅샷
    public record DeliveryInfo(
            UUID addressId,     // 배송지 고유 ID
            String address,     // 도로명 주소 스냅샷
            String detail       // 상세주소 스냅샷
    ) {}

    // 주문 메뉴 정보 스냅샷
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