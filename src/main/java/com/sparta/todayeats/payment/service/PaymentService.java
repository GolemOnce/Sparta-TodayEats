package com.sparta.todayeats.payment.service;

import com.sparta.todayeats.auth.application.service.AuthService;
import com.sparta.todayeats.global.exception.*;
import com.sparta.todayeats.global.service.UserAuthorizationService;
import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.order.repository.OrderRepository;
import com.sparta.todayeats.payment.dto.request.PaymentUpdateRequest;
import com.sparta.todayeats.payment.dto.response.*;
import com.sparta.todayeats.payment.entity.Payment;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import com.sparta.todayeats.payment.repository.PaymentRepository;
import com.sparta.todayeats.payment.dto.request.PaymentCreateRequest;
import com.sparta.todayeats.user.entity.User;
import com.sparta.todayeats.user.repository.UserRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    //
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserAuthorizationService userAuthorizationService;

    // 결제처리
    @Transactional
    public PaymentCreateResponse createPayment(UUID orderId, UUID userId, PaymentCreateRequest request) {
        // 1. 주문 조회 (비관적 락 적용)
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));

        // 2. 동일 주문에 대한 중복 결제 방지
        if (paymentRepository.findByOrder_OrderIdAndDeletedAtIsNull(orderId).isPresent()) {
            throw new BaseException(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        // 3. 본인 주문인지 검증
        if (!order.getCustomerId().equals(userId)) {
            throw new BaseException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        // 4. 주문 상태 검증
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new BaseException(OrderErrorCode.ORDER_ALREADY_CANCELLED);
        }

        // 5. 결제 엔티티 생성 (PENDING)
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalPrice())
                .build();
        paymentRepository.save(payment);

        // 6. 결제 처리 (실제 구현X)
        boolean success = true;

        // 7. payment status 갱신
        if (success) {
            payment.updatePaymentStatus(PaymentStatus.COMPLETED);
        } else {
            payment.updatePaymentStatus(PaymentStatus.CANCELLED);
        }

        return PaymentCreateResponse.builder()
                        .paymentId(payment.getId())
                        .orderId(orderId)
                        .amount(payment.getAmount())
                        .method(payment.getPaymentMethod())
                        .status(payment.getStatus())
                        .createdAt(payment.getCreatedAt())
                        .createdBy(payment.getCreatedBy())
                        .build();
    }

    // 목록 조회
    @Transactional(readOnly = true)
    public PaymentPageResponse getPagedPayments(UUID userId, @Nullable UUID targetUserId, Pageable pageable) {

        UUID queryId;

        if (targetUserId == null || userId.equals(targetUserId)) {
            // 본인 조회
            queryId = userId;
        } else {
            // 타인 조회 → 관리자 권한 필요
            User user = userAuthorizationService.getUserById(userId);
            userAuthorizationService.validateAdmin(user);
            queryId = targetUserId;
        }

        Page<Payment> payments = paymentRepository.findByUserId(queryId, pageable);
        return PaymentPageResponse.from(payments);
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetails(UUID userId, UUID paymentId) {

        // 1. 본인 결제내역으로 먼저 조회 시도
        Payment payment = paymentRepository.findByIdAndOrder_CustomerIdAndDeletedAtIsNull(paymentId, userId)
                .orElseGet(() -> {
                    // 2. 없으면 관리자 권한 확인 후 paymentId로만 재조회
                    User user = userAuthorizationService.getUserById(userId);
                    userAuthorizationService.validateAdmin(user); // 권한 없으면 여기서 예외
                    return paymentRepository.findById(paymentId)
                            .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));
                });

        // 3. 삭제된 결제내역 예외처리
        if (payment.isDeleted()) {
            throw new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }
        return PaymentDetailResponse.from(payment);
    }

    // 상태 수정
    @Transactional
    public PaymentUpdateResponse changePaymentStatus(UUID paymentId, UUID userId, PaymentUpdateRequest request) {
        // 1. 유저 권한 확인
        User user = userAuthorizationService.getUserById(userId);
        userAuthorizationService.validateAdmin(user); // 권한 없으면 예외

        // 2. 권한 통과 후 결제내역 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (payment.isDeleted()) {
            throw new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        payment.updatePaymentStatus(request.getStatus());
        return PaymentUpdateResponse.from(payment);
    }

    // 결제 삭제 (soft delete)
    @Transactional
    public void deletePayment(UUID paymentId, UUID userId) {
        // 1. 유저 권한 확인
        User user = userAuthorizationService.getUserById(userId);
        userAuthorizationService.validateMaster(user); // 권한 없으면 예외

        // 2. 권한 통과 후 soft delete
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        payment.softDelete(userId);
    }

    // 환불 처리 - 결제 상태를 CANCELLED로 변경 (실제 PG 연동 없이 DB 상태값만 변경)
    @Transactional
    public void refund(UUID orderId) {
        Payment payment = paymentRepository.findByOrder_OrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.updatePaymentStatus(PaymentStatus.CANCELLED);
    }
}
