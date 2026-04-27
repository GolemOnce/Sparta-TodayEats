package com.sparta.todayeats.payment.service;

import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.order.entity.OrderType;
import com.sparta.todayeats.order.repository.OrderRepository;
import com.sparta.todayeats.payment.dto.request.PaymentCreateRequest;
import com.sparta.todayeats.payment.dto.response.PaymentCreateResponse;
import com.sparta.todayeats.payment.dto.response.PaymentPageResponse;
import com.sparta.todayeats.payment.entity.Payment;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import com.sparta.todayeats.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    // Order 빌더 - status는 항상 PENDING으로 생성됨
    private Order buildOrder(UUID customerId, Long totalPrice) {
        return Order.builder()
                .customerId(customerId)
                .storeId(UUID.randomUUID())
                .addressId(UUID.randomUUID())
                .storeName("테스트 가게")
                .deliveryAddress("서울시 강남구")
                .deliveryDetail("101호")
                .orderType(OrderType.ONLINE)
                .totalPrice(totalPrice)
                .build();
    }

    // CANCELED 상태 주문은 빌더로 만들 수 없어서 ReflectionTestUtils 사용
    private Order buildCanceledOrder(UUID customerId) {
        Order order = buildOrder(customerId, 10000L);
        ReflectionTestUtils.setField(order, "status", OrderStatus.CANCELED);
        return order;
    }

    @Nested
    @DisplayName("결제 처리")
    class createPayment {
        @Test
        void 주문이_존재하지_않으면_예외() {
            // given
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            given(orderRepository.findByIdWithLock(orderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.createPayment(orderId, userId, new PaymentCreateRequest()))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 본인_주문이_아니면_예외() {
            // given
            UUID orderId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            Order order = buildOrder(ownerId, 10000L);
            given(orderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentService.createPayment(orderId, otherUserId, new PaymentCreateRequest()))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 취소된_주문은_결제_불가() {
            // given
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Order order = buildCanceledOrder(userId);
            given(orderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentService.createPayment(orderId, userId, new PaymentCreateRequest()))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 정상_결제시_COMPLETED_상태로_반환() {
            // given
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Order order = buildOrder(userId, 15000L);
            given(orderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));
            given(paymentRepository.save(any(Payment.class))).willAnswer(i -> i.getArgument(0));

            // when
            PaymentCreateResponse response = paymentService.createPayment(orderId, userId, new PaymentCreateRequest());

            // then
            assertThat(response.getAmount()).isEqualTo(15000L);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        void 정상_결제시_paymentRepository_save_호출() {
            // given
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Order order = buildOrder(userId, 15000L);
            given(orderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));
            given(paymentRepository.save(any(Payment.class))).willAnswer(i -> i.getArgument(0));

            // when
            paymentService.createPayment(orderId, userId, new PaymentCreateRequest());

            // then
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("결제 목록 조회")
    class getPagedPayments {
        @Test
        void 결제_목록_조회_성공() {
            // given
            UUID userId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            Order order = buildOrder(userId, 15000L);
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(15000L)
                    .build();

            Page<Payment> paymentPage = new PageImpl<>(List.of(payment), pageable, 1);
            given(paymentRepository.findByOrder_userId(userId, pageable)).willReturn(paymentPage);

            // when
            PaymentPageResponse response = paymentService.getPagedPayments(userId, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getPayments()).hasSize(1);
            assertThat(response.getPayments().get(0).getAmount()).isEqualTo(15000L);
        }

        @Test
        void 결제_내역_없으면_빈_페이지_반환() {
            // given
            UUID userId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            given(paymentRepository.findByOrder_userId(userId, pageable))
                    .willReturn(Page.empty(pageable));

            // when
            PaymentPageResponse response = paymentService.getPagedPayments(userId, pageable);

            // then
            assertThat(response.getPayments()).isEmpty();
        }
    }
}