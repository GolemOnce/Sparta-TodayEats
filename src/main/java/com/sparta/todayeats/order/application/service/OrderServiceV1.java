package com.sparta.todayeats.order.application.service;

import com.sparta.todayeats.address.domain.entity.AddressEntity;
import com.sparta.todayeats.address.domain.repository.AddressRepository;
import com.sparta.todayeats.global.exception.AddressErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.MenuErrorCode;
import com.sparta.todayeats.global.exception.StoreErrorCode;
import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import com.sparta.todayeats.menu.domain.repository.MenuRepository;
import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderItemEntity;
import com.sparta.todayeats.order.domain.repository.OrderRepository;
import com.sparta.todayeats.order.presentation.dto.request.CreateOrderRequest;
import com.sparta.todayeats.order.presentation.dto.response.CreateOrderResponse;
import com.sparta.todayeats.store.domain.entity.StoreEntity;
import com.sparta.todayeats.store.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderServiceV1 {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final AddressRepository addressRepository;

    // ========================================================
    // 커밋 3. ✨ feat: 주문 생성 서비스 로직 구현
    // ========================================================

    /**
     * 주문 생성
     * - 가게/배송지/메뉴 존재 검증
     * - 주문 시점 스냅샷 저장 (가게명, 배송지, 메뉴명, 단가)
     * - totalPrice 서버에서 계산 (클라이언트 값 신뢰 안 함)
     * - TODO: CUSTOMER 권한 체크 추가
     */
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, UUID userId) {
        // 가게 조회 및 검증
        StoreEntity store = storeRepository.findActiveById(request.storeId())
                .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));

        // 배송지 조회 및 검증 (주소 스냅샷용)
        AddressEntity address = addressRepository.findActiveById(request.addressId())
                .orElseThrow(() -> new BaseException(AddressErrorCode.ADDRESS_NOT_FOUND));

        // 주문 엔티티 생성
        OrderEntity order = OrderEntity.builder()
                .customerId(userId)
                .storeId(store.getStoreId())
                .addressId(address.getAddressId())
                .storeName(store.getName())             // 가게명 스냅샷
                .deliveryAddress(address.getAddress())  // 도로명 주소 스냅샷
                .deliveryDetail(address.getDetail())    // 상세주소 스냅샷
                .orderType(request.orderType())
                .note(request.note())
                .totalPrice(0L)                         // 아래에서 계산 후 갱신
                .build();

        long total = 0L;

        // 주문 항목 생성 및 totalPrice 계산
        for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {
            MenuEntity menu = menuRepository.findActiveById(itemReq.menuId())
                    .orElseThrow(() -> new BaseException(MenuErrorCode.MENU_NOT_FOUND));

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .menuId(menu.getMenuId())
                    .menuName(menu.getName())       // 메뉴명 스냅샷
                    .unitPrice(menu.getPrice())     // 단가 스냅샷
                    .quantity(itemReq.quantity())
                    .build();

            order.addOrderItem(orderItem);
            total += (long) menu.getPrice() * itemReq.quantity();
        }

        // 서버에서 계산한 총 금액 세팅
        order.updateTotalPrice(total);
        OrderEntity saved = orderRepository.save(order);

        log.info("주문 생성 완료: orderId={}, userId={}, total={}", saved.getOrderId(), userId, total);
        return CreateOrderResponse.from(saved);
    }
}