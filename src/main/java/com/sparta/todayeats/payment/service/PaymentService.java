package com.sparta.todayeats.payment.service;

import com.sparta.todayeats.category.presentation.dto.PageResponse;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.OrderErrorCode;
import com.sparta.todayeats.global.exception.PaymentErrorCode;
import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.order.repository.OrderRepository;
import com.sparta.todayeats.payment.dto.response.*;
import com.sparta.todayeats.payment.entity.Payment;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import com.sparta.todayeats.payment.repository.PaymentRepository;
import com.sparta.todayeats.payment.dto.request.PaymentCreateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    // 결제처리
    @Transactional
    public PaymentCreateResponse createPayment(UUID orderId, UUID userId, PaymentCreateRequest request) {
        // 1. 주문 조회 (비관적 락 적용)
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));

        // 2. 본인 주문인지 검증
        if (!order.getCustomerId().equals(userId)) {
            throw new BaseException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        // 3. 주문 상태 검증
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new BaseException(OrderErrorCode.ORDER_ALREADY_CANCELLED);
        }

        // 4. 결제 엔티티 생성 (PENDING)
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalPrice())
                .build();
        paymentRepository.save(payment);

        // 5. 결제 처리 (실제 구현X)
        boolean success = true;

        // 6. payment status 갱신
        if (success) {
            payment.updatePaymentStatus(PaymentStatus.COMPLETED);
        } else {
            payment.updatePaymentStatus(PaymentStatus.CANCELLED);
        }

        // 7. order status 갱신
         order.updateOrderStatus(OrderStatus.PENDING);

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
    public PaymentPageResponse getPagedPayments(UUID userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByOrder_userId(userId, pageable);
        return PaymentPageResponse.from(payments);
    }

    // 상세 조회

    // 상태 수정


    // 결제 삭제 (soft delete)

}
