package com.sparta.todayeats.order.service;

import com.sparta.todayeats.address.entity.Address;
import com.sparta.todayeats.address.repository.AddressRepository;
import com.sparta.todayeats.global.exception.*;
import com.sparta.todayeats.menu.entity.Menu;
import com.sparta.todayeats.menu.repository.MenuRepository;
import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderItem;
import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.order.repository.OrderRepository;
import com.sparta.todayeats.order.dto.request.*;
import com.sparta.todayeats.order.dto.response.*;
import com.sparta.todayeats.payment.dto.request.PaymentCreateRequest;
import com.sparta.todayeats.payment.dto.response.PaymentCreateResponse;
import com.sparta.todayeats.payment.entity.PaymentMethod;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import com.sparta.todayeats.payment.service.PaymentService;
import com.sparta.todayeats.store.entity.Store;
import com.sparta.todayeats.store.repository.StoreRepository;
import com.sparta.todayeats.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 주문 서비스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final AddressRepository addressRepository;
    private final PaymentService paymentService;

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
        Address address = addressRepository.findActiveById(request.addressId())
                .orElseThrow(() -> new BaseException(AddressErrorCode.ADDRESS_NOT_FOUND));
        if (!address.getUser().getUserId().equals(userId)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        // 주문 엔티티 생성
        Order order = Order.builder()
                .customerId(userId)
                .storeId(store.getId())
                .addressId(address.getId())
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
            Menu menu = menuRepository.findActiveById(itemReq.menuId())
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

        // 주문 생성과 함께 결제 처리 (기본 결제 수단: CARD)
        PaymentCreateResponse payment = paymentService.createPayment(
                saved.getOrderId(), userId, new PaymentCreateRequest(PaymentMethod.CARD));
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BaseException(PaymentErrorCode.PAYMENT_FAILED);
        }

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
            , UserRoleEnum role
    ) {
        if (role == UserRoleEnum.CUSTOMER) {
            return orderRepository.searchOrders(userId, status, storeName, pageable)
                    .map(OrderSummaryResponse::from);
        } else if (role == UserRoleEnum.OWNER) {
            return orderRepository.searchOrdersByStoreOwner(userId, status, storeName, pageable)
                    .map(OrderSummaryResponse::from);
        } else if (role == UserRoleEnum.MANAGER) {
            return orderRepository.searchAllOrders(status, storeName, pageable)
                    .map(OrderSummaryResponse::from);
        } else if (role == UserRoleEnum.MASTER) {
            return orderRepository.searchAllOrdersIncludeDeleted(status, storeName, pageable)
                    .map(OrderSummaryResponse::from);
        } else {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }
    }

    /**
     * 주문 단건 조회
     * - CUSTOMER: 본인 주문만 조회 가능(삭제된 주문 미포함)
     * - OWNER: 본인 가게 주문만 조회 가능(삭제된 주문 미포함)
     * - MANAGER: 전체 조회 가능(삭제된 주문 미포함)
     * - MASTER: 전체 조회 가능(삭제된 주문 포함)
     */
    public OrderDetailResponse getOrder(UUID orderId, UUID userId, UserRoleEnum role) {
        Order order = (role == UserRoleEnum.MASTER)
                ? orderRepository.findById(orderId)
                  .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND))
                : findActiveOrder(orderId);

        if (role == UserRoleEnum.CUSTOMER) {
            if (!order.getCustomerId().equals(userId)) {
                throw new BaseException(CommonErrorCode.FORBIDDEN);
            }
        } else if (role == UserRoleEnum.OWNER) {
            validateOwnerStoreAccess(order.getStoreId(), userId);
        } else if (role != UserRoleEnum.MANAGER && role != UserRoleEnum.MASTER) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        return OrderDetailResponse.from(order);
    }

    /**
     * 주문 요청사항 수정
     * - PENDING 상태만 수정 가능
     * - CUSTOMER 본인만 수정 가능
     */
    @Transactional
    public UpdateOrderResponse updateOrder(UUID orderId,
                                           UpdateOrderRequest request,
                                           UUID userId,
                                           UserRoleEnum role
    ) {
        if (role != UserRoleEnum.CUSTOMER) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        Order order = findActiveOrder(orderId);

        if (!order.getCustomerId().equals(userId)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        order.updateNote(request.note());  // 권한 체크 후 수정

        log.info("주문 요청사항 수정: orderId={}", orderId);
        return UpdateOrderResponse.from(order);
    }

    /**
     * 주문 상태 변경
     * - 허용된 상태 전이만 가능 (OrderStatus.validateTransition())
     * - CUSTOMER: 상태 변경 불가
     * - OWNER: 본인 가게 주문만 변경 가능
     * - MANAGER/MASTER: 전체 변경 가능
     */
    @Transactional
    public UpdateOrderStatusResponse updateOrderStatus(UUID orderId,
                                                       UpdateOrderStatusRequest request,
                                                       UUID userId,
                                                       UserRoleEnum role
    ) {
        if (role == UserRoleEnum.CUSTOMER) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        Order order = findActiveOrder(orderId);

        if (role == UserRoleEnum.OWNER) {
            validateOwnerStoreAccess(order.getStoreId(), userId);
        } else if (role != UserRoleEnum.MANAGER && role != UserRoleEnum.MASTER) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        order.validateStatusTransition(request.status()); // 검증만 (validateTransition)

        int rows = orderRepository.updateStatusConditionally(orderId, order.getStatus(), request.status(), userId);
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
     * - CUSTOMER 본인 또는 MASTER만 가능
     */
    @Transactional
    public CancelOrderResponse cancelOrder(UUID orderId,
                                           CancelOrderRequest request,
                                           UUID userId,
                                           UserRoleEnum role
    ) {
        if (role != UserRoleEnum.CUSTOMER && role != UserRoleEnum.MASTER) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        Order order = findActiveOrder(orderId);

        if (role == UserRoleEnum.CUSTOMER && !order.getCustomerId().equals(userId)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        order.cancelByCustomer();  // 검증만

        int rows = orderRepository.cancelConditionally(orderId, request != null ? request.cancelReason() : null, OrderStatus.PENDING.name(), OrderStatus.CANCELED.name(), userId);
        if (rows == 0) {
            Order freshOrder = findActiveOrder(orderId); // DB에서 재조회
            freshOrder.cancelByCustomer(); // 시간 초과면 CANCEL_TIME_EXCEEDED
            throw new BaseException(OrderErrorCode.ORDER_CONFLICT);
        }

        // 주문 취소 시 환불 처리 (결제 없는 기존 주문은 무시)
        try {
            paymentService.refund(orderId);
        } catch (BaseException e) {
            if (e.getErrorCode() != PaymentErrorCode.PAYMENT_NOT_FOUND) {
                throw e;
            }
            log.debug("No payment found for order {}, skipping refund", orderId);
        }

        Order updated = findActiveOrder(orderId);
        log.info("주문 취소: orderId={}", orderId);
        return CancelOrderResponse.from(updated);
    }

    /**
     * 주문 거절
     * - PENDING 상태만 거절 가능
     * - status = REJECTED 로 변경
     * - CUSTOMER: 거절 불가
     * - OWNER: 본인 가게 주문만 거절 가능
     * - MANAGER/MASTER: 전체 거절 가능
     */
    @Transactional
    public RejectOrderResponse rejectOrder(UUID orderId,
                                           RejectOrderRequest request,
                                           UUID userId,
                                           UserRoleEnum role
    ) {
        if (role == UserRoleEnum.CUSTOMER) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        Order order = findActiveOrder(orderId);

        if (role == UserRoleEnum.OWNER) {
            validateOwnerStoreAccess(order.getStoreId(), userId);
        } else if (role != UserRoleEnum.MANAGER && role != UserRoleEnum.MASTER) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        order.rejectByOwner();  // 검증만

        int rows = orderRepository.rejectConditionally(orderId, request != null ? request.rejectReason() : null, OrderStatus.PENDING, OrderStatus.REJECTED, userId);
        if (rows == 0) {
            throw new BaseException(OrderErrorCode.ORDER_CONFLICT);
        }

        // 주문 거절 시 환불 처리 (결제 없는 기존 주문은 무시)
        try {
            paymentService.refund(orderId);
        } catch (BaseException e) {
            if (e.getErrorCode() != PaymentErrorCode.PAYMENT_NOT_FOUND) {
                throw e;
            }
            log.debug("No payment found for order {}, skipping refund", orderId);
        }

        Order updated = findActiveOrder(orderId);
        log.info("주문 거절: orderId={}", orderId);
        return RejectOrderResponse.from(updated);
    }

    /**
     * 주문 삭제 (Soft delete)
     * - MASTER만 가능
     */
    @Transactional
    public void deleteOrder(UUID orderId,
                            UUID userId,
                            UserRoleEnum role
    ) {
        if (role != UserRoleEnum.MASTER) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        Order order = findActiveOrder(orderId);

        // 결제 완료 상태면 환불 처리 (결제 없는 기존 주문은 무시)
        try {
            paymentService.refund(orderId);
        } catch (BaseException e) {
            if (e.getErrorCode() != PaymentErrorCode.PAYMENT_NOT_FOUND) {
                throw e;
            }
            log.debug("No payment found for order {}, skipping refund", orderId);
        }

        order.delete(userId);

        log.info("주문 삭제: orderId={}", orderId);
    }

    /**
     * 사용자의 진행 중인 주문 존재 여부 반환
     * UserService에서 사용자 삭제 전 호출
     */
    public boolean hasActiveOrders(UUID userId) {
        return orderRepository.existsActiveOrderByCustomerId(userId);
    }

    /**
     * soft delete 제외 주문 단건 조회
     * 주문 없으면 BaseException(ORDER_NOT_FOUND) 발생
     */
    private Order findActiveOrder(UUID orderId) {
        return orderRepository.findActiveById(orderId)
                .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    /**
     * OWNER 가게 소유권 검증
     * 가게가 존재하지 않으면 STORE_NOT_FOUND, 본인 가게가 아니면 FORBIDDEN 발생
     *
     * @param storeId 검증할 가게 ID
     * @param userId  요청한 사용자 ID
     */
    private void validateOwnerStoreAccess(UUID storeId, UUID userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
        if (!store.getOwner().getUserId().equals(userId)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }
    }
}