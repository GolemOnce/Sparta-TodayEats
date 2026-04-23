package com.sparta.todayeats.order.application.service;

import com.sparta.todayeats.address.domain.entity.AddressEntity;
import com.sparta.todayeats.address.domain.repository.AddressRepository;
import com.sparta.todayeats.global.exception.*;
import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import com.sparta.todayeats.menu.domain.repository.MenuRepository;
import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderItemEntity;
import com.sparta.todayeats.order.domain.repository.OrderRepository;
import com.sparta.todayeats.order.presentation.dto.request.CreateOrderRequest;
import com.sparta.todayeats.order.presentation.dto.request.UpdateOrderRequest;
import com.sparta.todayeats.order.presentation.dto.request.UpdateOrderStatusRequest;
import com.sparta.todayeats.order.presentation.dto.response.*;
import com.sparta.todayeats.store.domain.entity.StoreEntity;
import com.sparta.todayeats.store.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    // feat: 주문 생성 서비스 로직 구현
    // ========================================================

    /**
     * 주문 생성
     * - 가게/배송지/메뉴 존재 검증
     * - 주문 시점 스냅샷 저장 (가게명, 배송지, 메뉴명, 단가)
     * - totalPrice 서버에서 계산 (클라이언트 값 신뢰 안 함)
     * TODO: JWT 완성 후 주석 해제
     * - CUSTOMER만 주문 생성 가능
     */
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, UUID userId
                                           //, UserRole role  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 주석 해제
        // if (role != UserRole.CUSTOMER) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }

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

    // ========================================================
    // feat: 주문 목록 조회 서비스 로직 추가
    // ========================================================

    /**
     * 주문 목록 조회
     * - soft delete 제외
     * - 페이지네이션 (기본 10개, createdAt DESC)
     * TODO: JWT 완성 후 주석 해제
     * - CUSTOMER: 본인 주문만 조회
     * - OWNER: 본인 가게 주문만 조회
     * - MANAGER/MASTER: 전체 조회
     */
    public Page<OrderSummaryResponse> getOrders(UUID userId, Pageable pageable
                                                //, UserRole role  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 주석 해제
        // if (role == UserRole.CUSTOMER) {
        //     return orderRepository.findAllByCustomerId(userId, pageable)
        //             .map(OrderSummaryResponse::from);
        // } else if (role == UserRole.OWNER) {
        //     return orderRepository.findAllByStoreOwnerId(userId, pageable)
        //             .map(OrderSummaryResponse::from);
        // }
        // // MANAGER/MASTER 전체 조회
        // return orderRepository.findAllActive(pageable)
        //         .map(OrderSummaryResponse::from);

        // 임시: JWT 완성 전까지 customerId로 조회
        return orderRepository.findAllByCustomerId(userId, pageable)
                .map(OrderSummaryResponse::from);
    }

    // ========================================================
    // feat: 주문 단건 조회 서비스 로직 추가
    // ========================================================

    /**
     * 주문 단건 조회
     * - soft delete 제외
     * TODO: JWT 완성 후 주석 해제
     * - CUSTOMER: 본인 주문만 조회 가능
     * - OWNER: 본인 가게 주문만 조회 가능
     * - MANAGER/MASTER: 전체 조회 가능
     */
    public OrderDetailResponse getOrder(UUID orderId
                                        //, UUID userId, UserRole role  // TODO: JWT 완성 후 주석 해제
    ) {
        OrderEntity order = findActiveOrder(orderId);

        // TODO: JWT 완성 후 주석 해제
        // if (role == UserRole.CUSTOMER) {
        //     if (!order.getCustomerId().equals(userId)) {
        //         throw new BaseException(CommonErrorCode.FORBIDDEN);
        //     }
        // } else if (role == UserRole.OWNER) {
        //     StoreEntity store = storeRepository.findActiveById(order.getStoreId())
        //             .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
        //     if (!store.getOwnerId().equals(userId)) {
        //         throw new BaseException(CommonErrorCode.FORBIDDEN);
        //     }
        // }

        return OrderDetailResponse.from(order);
    }

    // ========================================================
    // feat: 주문 수정 서비스 로직 추가
    // ========================================================

    /**
     * 주문 요청사항 수정
     * - PENDING 상태만 수정 가능
     * TODO: JWT 완성 후 주석 해제
     * - CUSTOMER 본인만 수정 가능
     */
    @Transactional
    public UpdateOrderResponse updateOrder(UUID orderId, UpdateOrderRequest request
                                           //, UUID userId, UserRole role  // TODO: JWT 완성 후 주석 해제
    ) {
        OrderEntity order = findActiveOrder(orderId);

        // TODO: JWT 완성 후 주석 해제
        // if (role != UserRole.CUSTOMER && role != UserRole.MASTER) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }
        // if (role == UserRole.CUSTOMER && !order.getCustomerId().equals(userId)) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }

        order.updateNote(request.note());  // 권한 체크 후 수정

        log.info("주문 요청사항 수정: orderId={}", orderId);
        return UpdateOrderResponse.from(order);
    }

    // ========================================================
    // feat: 주문 상태 변경 서비스 로직 추가
    // ========================================================

    /**
     * 주문 상태 변경
     * - 허용된 상태 전이만 가능 (OrderStatus.validateTransition())
     * TODO: JWT 완성 후 주석 해제
     * - OWNER: 본인 가게 주문만 변경 가능
     * - MANAGER/MASTER: 전체 변경 가능
     * - CUSTOMER: 상태 변경 불가
     */
    @Transactional
    public UpdateOrderStatusResponse updateOrderStatus(UUID orderId,
                                                       UpdateOrderStatusRequest request
                                                       //, UUID userId, UserRole role  // TODO: JWT 완성 후 주석 해제
    ) {
        OrderEntity order = findActiveOrder(orderId);

        // TODO: JWT 완성 후 주석 해제
        // if (role == UserRole.CUSTOMER) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // } else if (role == UserRole.OWNER) {
        //     StoreEntity store = storeRepository.findActiveById(order.getStoreId())
        //             .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
        //     if (!store.getOwnerId().equals(userId)) {
        //         throw new BaseException(CommonErrorCode.FORBIDDEN);
        //     }
        // }

        order.changeStatus(request.status());

        log.info("주문 상태 변경: orderId={}, status={}", orderId, request.status());
        return UpdateOrderStatusResponse.from(order);
    }

    // ========================================================
    // feat: 주문 취소 서비스 로직 추가
    // ========================================================

    /**
     * 주문 취소
     * - PENDING 상태에서 5분 이내만 가능
     * - status = CANCELED 로 변경 (soft delete 안 함 → 목록에 보임)
     * TODO: JWT 완성 후 주석 해제
     * - CUSTOMER 본인 또는 MASTER만 가능
     */
    @Transactional
    public CancelOrderResponse cancelOrder(UUID orderId
                                           //, UUID userId, UserRole role  // TODO: JWT 완성 후 주석 해제
    ) {
        OrderEntity order = findActiveOrder(orderId);

        // TODO: JWT 완성 후 주석 해제
        // if (role != UserRole.CUSTOMER && role != UserRole.MASTER) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }
        // if (role == UserRole.CUSTOMER && !order.getCustomerId().equals(userId)) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }

        order.cancelByCustomer();

        log.info("주문 취소: orderId={}", orderId);
        return CancelOrderResponse.from(order);
    }


    /**
     * soft delete 제외 주문 단건 조회
     * 주문 없으면 BaseException(ORDER_NOT_FOUND) 발생
     */
    private OrderEntity findActiveOrder(UUID orderId) {
        return orderRepository.findActiveById(orderId)
                .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));
    }
}