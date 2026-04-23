package com.sparta.todayeats.order.application.service;

import com.sparta.todayeats.address.domain.entity.AddressEntity;
import com.sparta.todayeats.address.domain.repository.AddressRepository;
import com.sparta.todayeats.global.exception.*;
import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import com.sparta.todayeats.menu.domain.repository.MenuRepository;
import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderStatus;
import com.sparta.todayeats.order.domain.entity.OrderType;
import com.sparta.todayeats.order.domain.repository.OrderRepository;
import com.sparta.todayeats.order.presentation.dto.request.CreateOrderRequest;
import com.sparta.todayeats.order.presentation.dto.request.UpdateOrderRequest;
import com.sparta.todayeats.order.presentation.dto.request.UpdateOrderStatusRequest;
import com.sparta.todayeats.order.presentation.dto.response.*;
import com.sparta.todayeats.store.domain.entity.StoreEntity;
import com.sparta.todayeats.store.domain.repository.StoreRepository;
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

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("OrderServiceV1 테스트")
@ExtendWith(MockitoExtension.class)
class OrderServiceV1Test {

    @InjectMocks
    private OrderServiceV1 orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AddressRepository addressRepository;

    // ─── 테스트 픽스처 ────────────────────────────────────────────────────

    private final UUID userId    = UUID.randomUUID();
    private final UUID storeId   = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();
    private final UUID menuId    = UUID.randomUUID();
    private final UUID orderId   = UUID.randomUUID();

    private CreateOrderRequest createOrderRequest() {
        return new CreateOrderRequest(
                storeId,
                addressId,
                OrderType.ONLINE,
                "문 앞에 놔주세요",
                List.of(new CreateOrderRequest.OrderItemRequest(menuId, 2))
        );
    }

    // ========================================================
    // 🎥 test(#9): 주문 생성 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("성공 - 주문 생성 및 totalPrice 서버 계산")
        void success() {
            // given
            given(storeRepository.findActiveById(storeId))
                    .willReturn(Optional.of(mock(StoreEntity.class)));
            given(addressRepository.findActiveById(addressId))
                    .willReturn(Optional.of(mock(AddressEntity.class)));
            given(menuRepository.findActiveById(menuId))
                    .willReturn(Optional.of(mock(MenuEntity.class)));
            given(orderRepository.save(any(OrderEntity.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // when
            CreateOrderResponse result = orderService.createOrder(createOrderRequest(), userId);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.items()).hasSize(1);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 가게")
        void fail_store_not_found() {
            // given
            given(storeRepository.findActiveById(storeId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(), userId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(StoreErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 배송지")
        void fail_address_not_found() {
            // given
            given(storeRepository.findActiveById(storeId))
                    .willReturn(Optional.of(mock(StoreEntity.class)));
            given(addressRepository.findActiveById(addressId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(), userId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AddressErrorCode.ADDRESS_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 메뉴")
        void fail_menu_not_found() {
            // given
            given(storeRepository.findActiveById(storeId))
                    .willReturn(Optional.of(mock(StoreEntity.class)));
            given(addressRepository.findActiveById(addressId))
                    .willReturn(Optional.of(mock(AddressEntity.class)));
            given(menuRepository.findActiveById(menuId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(), userId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(MenuErrorCode.MENU_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 숨김 처리된 메뉴 주문 불가")
        void fail_hidden_menu() {
            // given
            given(storeRepository.findActiveById(storeId))
                    .willReturn(Optional.of(mock(StoreEntity.class)));
            given(addressRepository.findActiveById(addressId))
                    .willReturn(Optional.of(mock(AddressEntity.class)));
            // is_hidden = true인 메뉴는 findActiveById에서 제외되어 empty 반환
            given(menuRepository.findActiveById(menuId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(), userId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(MenuErrorCode.MENU_NOT_FOUND));
        }
    }

    // ========================================================
    // 🎥 test(#9): 주문 목록 조회 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("getOrders()")
    class GetOrders {

        @Test
        @DisplayName("성공 - 본인 주문 목록 조회")
        void success() {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            Page<OrderEntity> page = new PageImpl<>(List.of(order));
            given(orderRepository.findAllByCustomerId(eq(userId), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(userId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).storeName()).isEqualTo("BBQ 광화문점");
            assertThat(result.getContent().get(0).totalPrice()).isEqualTo(38000L);
            assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("성공 - 주문 없을 때 빈 목록 반환")
        void success_empty() {
            // given
            Page<OrderEntity> emptyPage = new PageImpl<>(List.of());
            given(orderRepository.findAllByCustomerId(eq(userId), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(userId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("실패 - soft delete된 주문은 목록에 포함되지 않음")
        void fail_deleted_order_not_included() {
            // given
            // soft delete된 주문은 Repository 쿼리에서 이미 제외됨
            // deletedAt IS NULL 조건으로 필터링
            Page<OrderEntity> emptyPage = new PageImpl<>(List.of());
            given(orderRepository.findAllByCustomerId(eq(userId), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(userId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
            verify(orderRepository).findAllByCustomerId(eq(userId), any(Pageable.class));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 주문은 조회되지 않음")
        void fail_other_user_order_not_included() {
            // given
            UUID otherUserId = UUID.randomUUID();
            Page<OrderEntity> emptyPage = new PageImpl<>(List.of());
            given(orderRepository.findAllByCustomerId(eq(otherUserId), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(otherUserId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
            // userId로 조회하면 otherUserId 주문은 나오지 않음
            verify(orderRepository, never()).findAllByCustomerId(eq(userId), any(Pageable.class));
        }
    }

    // ========================================================
    // 🎥 test(#9): 주문 단건 조회 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("getOrder()")
    class GetOrder {

        @Test
        @DisplayName("성공 - 주문 단건 조회")
        void success() {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when
            OrderDetailResponse result = orderService.getOrder(orderId);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.store().storeName()).isEqualTo("BBQ 광화문점");
            assertThat(result.totalPrice()).isEqualTo(38000L);
            assertThat(result.delivery().address()).isEqualTo("서울 광화문 100번지");
        }

        @Test
        @DisplayName("실패 - 주문 없음 (존재하지 않거나 삭제된 주문)")
        void fail_order_not_found() {
            // given
            // 존재하지 않는 주문 또는 soft delete된 주문 모두
            // findActiveById에서 Optional.empty() 반환
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrder(orderId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }
    }

    // ========================================================
    // 🎥 test(#9): 주문 수정 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("updateOrder()")
    class UpdateOrder {

        @Test
        @DisplayName("성공 - PENDING 상태에서 요청사항 수정")
        void success() {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when
            UpdateOrderResponse result = orderService.updateOrder(
                    orderId, new UpdateOrderRequest("수정된 요청사항"));

            // then
            assertThat(result.note()).isEqualTo("수정된 요청사항");
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문")
        void fail_order_not_found() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.updateOrder(
                    orderId, new UpdateOrderRequest("수정된 요청사항")))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - PENDING이 아닌 상태에서 수정 시도")
        void fail_not_pending_status() throws Exception {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            order.changeStatus(OrderStatus.ACCEPTED); // ACCEPTED 상태로 변경
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.updateOrder(
                    orderId, new UpdateOrderRequest("수정된 요청사항")))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_UPDATE_NOT_ALLOWED));
        }
    }

    // ========================================================
    // 🎥 test(#9): 주문 상태 변경 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("updateOrderStatus()")
    class UpdateOrderStatus {

        @Test
        @DisplayName("성공 - PENDING → ACCEPTED 상태 전이")
        void success_pending_to_accepted() {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when
            UpdateOrderStatusResponse result = orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.ACCEPTED));

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.ACCEPTED);
        }

        @Test
        @DisplayName("성공 - ACCEPTED → COOKING 상태 전이")
        void success_accepted_to_cooking() {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            order.changeStatus(OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when
            UpdateOrderStatusResponse result = orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.COOKING));

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.COOKING);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문")
        void fail_order_not_found() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.ACCEPTED)))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 허용되지 않는 상태 전이 (PENDING → COMPLETED)")
        void fail_invalid_transition() {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.COMPLETED)))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.INVALID_ORDER_STATUS));
        }

        @Test
        @DisplayName("실패 - 역방향 상태 전이 (ACCEPTED → PENDING)")
        void fail_reverse_transition() {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            order.changeStatus(OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.PENDING)))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.INVALID_ORDER_STATUS));
        }
    }

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        @Test
        @DisplayName("성공 - 주문 후 5분 이내 취소")
        void success_cancel_within_5_minutes() throws Exception {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            // 3분 전 생성된 주문 (5분 이내)
            Field field = order.getClass().getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(order, LocalDateTime.now().minusMinutes(3));

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when
            CancelOrderResponse result = orderService.cancelOrder(orderId);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("실패 - 주문 후 5분 초과")
        void fail_cancel_after_5_minutes() throws Exception {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            // 6분 전 생성된 주문 (5분 초과)
            Field field = order.getClass().getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(order, LocalDateTime.now().minusMinutes(6));

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.CANCEL_TIME_EXCEEDED));
        }

        @Test
        @DisplayName("실패 - PENDING이 아닌 상태에서 취소 시도")
        void fail_cancel_non_pending_order() throws Exception {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            Field field = order.getClass().getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(order, LocalDateTime.now().minusMinutes(1));

            order.changeStatus(OrderStatus.ACCEPTED); // ACCEPTED 상태로 변경
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED));
        }

        @Test
        @DisplayName("실패 - 5분 이내지만 PENDING이 아닌 상태")
        void fail_cancel_not_pending_within_5_minutes() throws Exception {
            // given
            OrderEntity order = OrderEntity.builder()
                    .customerId(userId)
                    .storeId(storeId)
                    .addressId(addressId)
                    .storeName("BBQ 광화문점")
                    .deliveryAddress("서울 광화문 100번지")
                    .deliveryDetail("101호")
                    .orderType(OrderType.ONLINE)
                    .note("문 앞에 놔주세요")
                    .totalPrice(38000L)
                    .build();

            // 3분 전 생성 (5분 이내)
            Field field = order.getClass().getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(order, LocalDateTime.now().minusMinutes(3));

            order.changeStatus(OrderStatus.ACCEPTED);  // ACCEPTED 상태로 변경
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문")
        void fail_order_not_found() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }
    }
}