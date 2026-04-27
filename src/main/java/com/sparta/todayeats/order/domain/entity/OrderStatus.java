package com.sparta.todayeats.order.domain.entity;

import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.OrderErrorCode;

import java.util.List;
import java.util.Map;

/** 주문 상태 */
public enum OrderStatus {

    PENDING,    // 주문요청 (CUSTOMER)
    ACCEPTED,   // 주문수락 (OWNER)
    COOKING,    // 조리완료 (OWNER)
    DELIVERING, // 배송수령 (OWNER)
    DELIVERED,  // 배송완료 (OWNER)
    COMPLETED,  // 주문완료 (OWNER)
    CANCELED,   // 취소 (CUSTOMER/MASTER)
    REJECTED;   // 거절 (OWNER)

    /**
     * 허용된 상태 전이 규칙표
     * 각 상태에서 이동할 수 있는 다음 상태만 정의
     * 정의되지 않은 전이는 validateTransition()에서 차단
     * PENDING    → ACCEPTED, CANCELED, REJECTED 만 가능
     * ACCEPTED   → COOKING 만 가능
     * COOKING    → DELIVERING 만 가능
     * DELIVERING → DELIVERED 만 가능
     * DELIVERED  → COMPLETED 만 가능
     * COMPLETED  → 종료 상태 (이동 불가)
     * CANCELED   → 종료 상태 (이동 불가)
     * REJECTED   → 종료 상태 (이동 불가)
     */
    private static final Map<OrderStatus, List<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING,    List.of(ACCEPTED, CANCELED, REJECTED),
            ACCEPTED,   List.of(COOKING),
            COOKING,    List.of(DELIVERING),
            DELIVERING, List.of(DELIVERED),
            DELIVERED,  List.of(COMPLETED)
    );

    /**
     * 상태 전이 유효성 검증
     * PATCH /api/v1/orders/{orderId} 호출 시 OrderEntity.changeStatus()에서 사용
     * 허용되지 않은 전이면 BaseException(INVALID_ORDER_STATUS) 발생
     * 예시)
     * PENDING → ACCEPTED  ✅ 허용
     * PENDING → REJECTED  ✅ 허용
     * PENDING → COMPLETED ❌ 차단
     * COMPLETED → PENDING ❌ 차단 (역방향 불가)
     */
    public void validateTransition(OrderStatus next) {
        List<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(this, List.of());
        if (!allowed.contains(next)) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_STATUS);
        }
    }
}
