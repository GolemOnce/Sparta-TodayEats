package com.sparta.todayeats.order.service;

import com.sparta.todayeats.address.domain.entity.AddressEntity;
import com.sparta.todayeats.address.domain.repository.AddressRepository;
import com.sparta.todayeats.global.exception.*;
import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import com.sparta.todayeats.menu.domain.repository.MenuRepository;
import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderItem;
import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.order.repository.OrderRepository;
import com.sparta.todayeats.order.dto.request.*;
import com.sparta.todayeats.order.dto.response.*;
import com.sparta.todayeats.store.entity.Store;
import com.sparta.todayeats.store.repository.StoreRepository;
import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** 주문 서비스 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final AddressRepository addressRepository;

    /**
     * 주문 생성
     * - 가게/배송지/메뉴 존재 검증
     * - 주문 시점 스냅샷 저장 (가게명, 배송지, 메뉴명, 단가)
     * - totalPrice 서버에서 계산 (클라이언트 값 신뢰 안 함)
     * - CUSTOMER만 주문 생성 가능
     */
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, UUID userId, UserRoleEnum role) {
        if (role != UserRoleEnum.CUSTOMER) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        // 가게 조회 및 검증
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
        if (store.getIsHidden()) {
            throw new BaseException(StoreErrorCode.STORE_NOT_FOUND);
        }

        // 배송지 조회 및 검증 (주소 스냅샷용)
        AddressEntity address = addressRepository.findActiveById(request.addressId())
                .orElseThrow(() -> new BaseException(AddressErrorCode.ADDRESS_NOT_FOUND));
        if (!address.getUserId().equals(userId)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        // 주문 엔티티 생성
        Order order = Order.builder()
                .customerId(userId)
                .storeId(store.getId())
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

            // 메뉴가 해당 가게 소속인지 검증
            if (!menu.getStoreId().equals(store.getId())) {
                throw new BaseException(MenuErrorCode.MENU_NOT_IN_STORE);
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuId(menu.getMenuId())
                    .menuName(menu.getName())       // 메뉴명 스냅샷
                    .unitPrice(menu.getPrice())     // 단가 스냅샷
                    .quantity(itemReq.quantity())
                    .build();

            order.addOrderItem(orderItem);
            total = Math.addExact(total, Math.multiplyExact(menu.getPrice(), (long) itemReq.quantity()));
        }

        // 서버에서 계산한 총 금액 세팅
        order.updateTotalPrice(total);
        Order saved = orderRepository.save(order);

        // TODO: Payment 담당자 코드 완성 후 주석 해제
        // 주문 생성 시 결제 생성 같이 처리 (트랜잭션 묶음)
        // paymentService.createPayment(saved.getOrderId(), total);

        log.info("주문 생성 완료: orderId={}, userId={}, total={}", saved.getOrderId(), userId, total);
        return CreateOrderResponse.from(saved);
    }

    /**
     * 주문 목록 조회
     * - soft delete 제외
     * - 페이지네이션 (기본 10개, createdAt DESC)
     * - CUSTOMER: 본인 주문만 조회
     * - OWNER: 본인 가게 주문만 조회
     * - MANAGER: 전체 조회 (soft delete 제외)
     * - MASTER: 전체 조회 (삭제 포함)
     */
    public Page<OrderSummaryResponse> getOrders(UUID userId,
                                                OrderStatus status,
                                                String storeName,
                                                Pageable pageable
                                                ,UserRoleEnum role
    ) {
        if (role == UserRoleEnum.CUSTOMER) {
            return orderRepository.searchOrders(userId, status, storeName, pageable)
                    .map(OrderSummaryResponse::from);
        } else if (role == UserRoleEnum.OWNER) {
            return orderRepository.findAllByStoreOwnerId(userId, pageable)
                    .map(OrderSummaryResponse::from);
        } else if (role == UserRoleEnum.MANAGER) {
            return orderRepository.searchAllOrders(status, storeName, pageable)
                    .map(OrderSummaryResponse::from);
        } else {
            return orderRepository.searchAllOrdersIncludeDeleted(status, storeName, pageable)
                    .map(OrderSummaryResponse::from);
        }
    }

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
        Order order = findActiveOrder(orderId);

        // TODO: JWT 완성 후 주석 해제
        // if (role == UserRole.CUSTOMER) {
        //     if (!order.getCustomerId().equals(userId)) {
        //         throw new BaseException(CommonErrorCode.FORBIDDEN);
        //     }
        // } else if (role == UserRole.OWNER) {
        //     StoreEntity store = storeRepository.findActiveById(order.getStoreId())
        //             .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
        //     if (!store.getOwner().getId().equals(userId)) {
        //         throw new BaseException(CommonErrorCode.FORBIDDEN);
        //     }
        // }

        return OrderDetailResponse.from(order);
    }

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
        Order order = findActiveOrder(orderId);

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
        Order order = findActiveOrder(orderId);

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

        order.validateStatusTransition(request.status()); // 검증만 (validateTransition)

        int rows = orderRepository.updateStatusConditionally(orderId, order.getStatus(), request.status());
        if (rows == 0) {
            throw new BaseException(OrderErrorCode.ORDER_CONFLICT);
        }

        Order updated = findActiveOrder(orderId);
        log.info("주문 상태 변경: orderId={}, status={}", orderId, request.status());
        return UpdateOrderStatusResponse.from(updated);
    }

    /**
     * 주문 취소
     * - PENDING 상태에서 5분 이내만 가능
     * - status = CANCELED 로 변경 (soft delete 안 함 → 목록에 보임)
     * TODO: JWT 완성 후 주석 해제
     * - CUSTOMER 본인 또는 MASTER만 가능
     */
    @Transactional
    public CancelOrderResponse cancelOrder(UUID orderId, CancelOrderRequest request
                                           //, UUID userId, UserRole role  // TODO: JWT 완성 후 주석 해제
    ) {
        Order order = findActiveOrder(orderId);

        // TODO: JWT 완성 후 주석 해제
        // if (role != UserRole.CUSTOMER && role != UserRole.MASTER) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }
        // if (role == UserRole.CUSTOMER && !order.getCustomerId().equals(userId)) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }

        order.cancelByCustomer();  // 검증만

        int rows = orderRepository.cancelConditionally(orderId, request != null ? request.cancelReason() : null, OrderStatus.PENDING.name(), OrderStatus.CANCELED.name());
        if (rows == 0) {
            throw new BaseException(OrderErrorCode.ORDER_CONFLICT);
        }

        // TODO: Payment 코드 완성 후 주석 해제
        // 주문 취소 시 환불 처리 같이 처리 (트랜잭션 묶음)
        // paymentService.refund(orderId);

        Order updated = findActiveOrder(orderId);
        log.info("주문 취소: orderId={}", orderId);
        return CancelOrderResponse.from(updated);
    }

    /**
     * 주문 거절
     * - PENDING 상태만 거절 가능
     * - status = REJECTED 로 변경
     * TODO: JWT 완성 후 주석 해제
     * - OWNER: 본인 가게 주문만 거절 가능
     * - MANAGER/MASTER: 전체 거절 가능
     * - CUSTOMER: 거절 불가
     */
    @Transactional
    public RejectOrderResponse rejectOrder(UUID orderId, RejectOrderRequest request
                                           //, UUID userId, UserRole role  // TODO: JWT 완성 후 주석 해제
    ) {
        Order order = findActiveOrder(orderId);

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

        order.rejectByOwner();  // 검증만

        int rows = orderRepository.rejectConditionally(orderId, request != null ? request.rejectReason() : null, OrderStatus.PENDING, OrderStatus.REJECTED);
        if (rows == 0) {
            throw new BaseException(OrderErrorCode.ORDER_CONFLICT);
        }

        // TODO: Payment 코드 완성 후 주석 해제
        // 주문 거절 시 환불 처리 같이 처리 (트랜잭션 묶음)
        // paymentService.refund(orderId);

        Order updated = findActiveOrder(orderId);
        log.info("주문 거절: orderId={}", orderId);
        return RejectOrderResponse.from(updated);
    }

    /**
     * 주문 삭제 (Soft delete)
     * - MASTER만 가능
     * TODO: JWT 완성 후 주석 해제
     */
    @Transactional
    public void deleteOrder(UUID orderId
                            //, UUID userId, UserRole role  // TODO: JWT 완성 후 주석 해제
    ) {
        Order order = findActiveOrder(orderId);

        // TODO: JWT 완성 후 주석 해제
        // if (role != UserRole.MASTER) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }

        // TODO: Payment 코드 완성 후 주석 해제
        // 결제 완료 상태면 환불 처리
        // paymentService.refundIfPaid(orderId);


        order.delete(null);  // TODO: JWT 완성 후 userId로 교체

        log.info("주문 삭제: orderId={}", orderId);
    }

    /**
     * soft delete 제외 주문 단건 조회
     * 주문 없으면 BaseException(ORDER_NOT_FOUND) 발생
     */
    private Order findActiveOrder(UUID orderId) {
        return orderRepository.findActiveById(orderId)
                .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));
    }
}