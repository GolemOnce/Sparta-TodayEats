package com.sparta.todayeats.payment.service;

import com.sparta.todayeats.global.exception.AuthErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.service.UserAuthorizationService;
import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.order.entity.OrderType;
import com.sparta.todayeats.order.repository.OrderRepository;
import com.sparta.todayeats.payment.dto.request.PaymentCreateRequest;
import com.sparta.todayeats.payment.dto.request.PaymentUpdateRequest;
import com.sparta.todayeats.payment.dto.response.PaymentCreateResponse;
import com.sparta.todayeats.payment.dto.response.PaymentDetailResponse;
import com.sparta.todayeats.payment.dto.response.PaymentPageResponse;
import com.sparta.todayeats.payment.dto.response.PaymentUpdateResponse;
import com.sparta.todayeats.payment.entity.Payment;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import com.sparta.todayeats.payment.repository.PaymentRepository;
import com.sparta.todayeats.user.domain.entity.User;
import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @InjectMocks
    private PaymentService paymentService;

    // Order 빌더 - status는 항상 PENDING으로 생성됨
    private Order buildOrder(UUID customerId, Long totalPrice) {
        return Order.builder()
                .customerId(customerId)
                .storeId(UUID.randomUUID())
                .storeName("테스트 가게")
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
            given(paymentRepository.findByUserId(userId, pageable)).willReturn(paymentPage);

            // when
            PaymentPageResponse response = paymentService.getPagedPayments(userId, null, pageable);

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

            given(paymentRepository.findByUserId(userId, pageable))
                    .willReturn(Page.empty(pageable));

            // when
            PaymentPageResponse response = paymentService.getPagedPayments(userId, null, pageable);

            // then
            assertThat(response.getPayments()).isEmpty();
        }
    }

    @Nested
    @DisplayName("결제 상세 조회")
    class getPaymentDetails {

        @Test
        void 본인_결제내역_조회_성공() {
            // given
            UUID userId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            Order order = buildOrder(userId, 15000L);
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(15000L)
                    .build();

            given(paymentRepository.findByIdAndOrder_CustomerId(paymentId, userId))
                    .willReturn(Optional.of(payment));

            // when
            PaymentDetailResponse response = paymentService.getPaymentDetails(userId, paymentId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAmount()).isEqualTo(15000L);
        }

        @Test
        void 관리자가_타인_결제내역_조회_성공() {
            // given
            UUID adminId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            Order order = buildOrder(customerId, 15000L);
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(15000L)
                    .build();

            User admin = User.builder().role(UserRoleEnum.MASTER).build();

            given(paymentRepository.findByIdAndOrder_CustomerId(paymentId, adminId))
                    .willReturn(Optional.empty());
            given(userAuthorizationService.getUserById(adminId))
                    .willReturn(admin);
            given(paymentRepository.findById(paymentId))
                    .willReturn(Optional.of(payment));

            // when
            PaymentDetailResponse response = paymentService.getPaymentDetails(adminId, paymentId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAmount()).isEqualTo(15000L);
            verify(userAuthorizationService).validateAdmin(admin);
        }

        @Test
        void 일반_유저가_타인_결제내역_조회시_예외() {
            // given
            UUID otherUserId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            User normalUser = User.builder().role(UserRoleEnum.CUSTOMER).build();

            given(paymentRepository.findByIdAndOrder_CustomerId(paymentId, otherUserId))
                    .willReturn(Optional.empty());
            given(userAuthorizationService.getUserById(otherUserId))
                    .willReturn(normalUser);
            doThrow(new BaseException(AuthErrorCode.FORBIDDEN))
                    .when(userAuthorizationService).validateAdmin(normalUser);

            // when & then
            assertThatThrownBy(() -> paymentService.getPaymentDetails(otherUserId, paymentId))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 존재하지_않는_결제내역_조회시_예외() {
            // given
            UUID adminId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            User admin = User.builder().role(UserRoleEnum.MASTER).build();

            given(paymentRepository.findByIdAndOrder_CustomerId(paymentId, adminId))
                    .willReturn(Optional.empty());
            given(userAuthorizationService.getUserById(adminId))
                    .willReturn(admin);
            given(paymentRepository.findById(paymentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getPaymentDetails(adminId, paymentId))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("결제 상태 변경")
    class changePaymentStatus {

        @Test
        void 관리자_결제_상태_변경_성공() {
            // given
            UUID adminId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            User admin = User.builder().role(UserRoleEnum.MASTER).build();
            Order order = buildOrder(UUID.randomUUID(), 15000L);
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(15000L)
                    .build();

            PaymentUpdateRequest request = new PaymentUpdateRequest(PaymentStatus.COMPLETED);

            given(userAuthorizationService.getUserById(adminId)).willReturn(admin);
            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

            // when
            PaymentUpdateResponse response = paymentService.changePaymentStatus(paymentId, adminId, request);

            // then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            verify(userAuthorizationService).validateAdmin(admin);
        }

        @Test
        void 일반_유저가_상태_변경_시도시_예외() {
            // given
            UUID userId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            User normalUser = User.builder().role(UserRoleEnum.CUSTOMER).build();
            PaymentUpdateRequest request = new PaymentUpdateRequest(PaymentStatus.COMPLETED);

            given(userAuthorizationService.getUserById(userId)).willReturn(normalUser);
            doThrow(new BaseException(AuthErrorCode.FORBIDDEN))
                    .when(userAuthorizationService).validateAdmin(normalUser);

            // when & then
            assertThatThrownBy(() -> paymentService.changePaymentStatus(paymentId, userId, request))
                    .isInstanceOf(BaseException.class);

            verify(paymentRepository, never()).findById(any()); // 권한 없으면 DB 조회 안 함
        }

        @Test
        void 존재하지_않는_결제내역_상태_변경시_예외() {
            // given
            UUID adminId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            User admin = User.builder().role(UserRoleEnum.MASTER).build();
            PaymentUpdateRequest request = new PaymentUpdateRequest(PaymentStatus.COMPLETED);

            given(userAuthorizationService.getUserById(adminId)).willReturn(admin);
            given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.changePaymentStatus(paymentId, adminId, request))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("결제 삭제")
    class deletePayment {

        @Test
        void MASTER_결제_삭제_성공() {
            // given
            UUID masterId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            User master = User.builder().role(UserRoleEnum.MASTER).build();
            Order order = buildOrder(UUID.randomUUID(), 15000L);
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(15000L)
                    .build();

            given(userAuthorizationService.getUserById(masterId)).willReturn(master);
            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

            // when
            paymentService.deletePayment(paymentId, masterId);

            // then
            assertThat(payment.isDeleted()).isTrue();
            assertThat(payment.getDeletedBy()).isEqualTo(masterId);
            verify(userAuthorizationService).validateMaster(master);
        }

        @Test
        void 일반_유저가_삭제_시도시_예외() {
            // given
            UUID userId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            User normalUser = User.builder().role(UserRoleEnum.CUSTOMER).build();

            given(userAuthorizationService.getUserById(userId)).willReturn(normalUser);
            doThrow(new BaseException(AuthErrorCode.FORBIDDEN))
                    .when(userAuthorizationService).validateMaster(normalUser);

            // when & then
            assertThatThrownBy(() -> paymentService.deletePayment(paymentId, userId))
                    .isInstanceOf(BaseException.class);

            verify(paymentRepository, never()).findById(any()); // 권한 없으면 DB 조회 안 함
        }

        @Test
        void 존재하지_않는_결제내역_삭제시_예외() {
            // given
            UUID masterId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            User master = User.builder().role(UserRoleEnum.MASTER).build();

            given(userAuthorizationService.getUserById(masterId)).willReturn(master);
            given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.deletePayment(paymentId, masterId))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 이미_삭제된_결제내역은_재삭제_안됨() {
            // given
            UUID masterId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            User master = User.builder().role(UserRoleEnum.MASTER).build();
            Order order = buildOrder(UUID.randomUUID(), 15000L);
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(15000L)
                    .build();

            payment.softDelete(masterId); // 미리 삭제된 상태
            LocalDateTime firstDeletedAt = payment.getDeletedAt();

            given(userAuthorizationService.getUserById(masterId)).willReturn(master);
            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

            // when
            paymentService.deletePayment(paymentId, masterId);

            // then
            assertThat(payment.getDeletedAt()).isEqualTo(firstDeletedAt); // deletedAt 변경 없음
        }
    }
}