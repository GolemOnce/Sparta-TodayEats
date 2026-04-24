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
import com.sparta.todayeats.order.presentation.dto.request.*;
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
import static org.mockito.ArgumentMatchers.isNull;
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

    private OrderEntity pendingOrder() {
        return OrderEntity.builder()
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
    }

    private void setCreatedAt(OrderEntity order, LocalDateTime time) throws Exception {
        Field field = order.getClass().getSuperclass().getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(order, time);
    }

    // ========================================================
    // 🎥 test(#9): 주문 생성 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("성공 - 주문 생성 및 totalPrice 서버 계산")
        void 주문_생성_성공() {
            // given
            MenuEntity mockMenu = mock(MenuEntity.class);
            given(mockMenu.getPrice()).willReturn(18000L);
            given(mockMenu.getName()).willReturn("황금올리브 치킨");
            given(mockMenu.getStoreId()).willReturn(storeId);

            StoreEntity mockStore = mock(StoreEntity.class);
            given(mockStore.getStoreId()).willReturn(storeId);

            given(storeRepository.findActiveById(storeId))
                    .willReturn(Optional.of(mockStore));
            given(addressRepository.findActiveById(addressId))
                    .willReturn(Optional.of(mock(AddressEntity.class)));
            given(menuRepository.findActiveById(menuId))
                    .willReturn(Optional.of(mockMenu));
            given(orderRepository.save(any(OrderEntity.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // when
            CreateOrderResponse result = orderService.createOrder(createOrderRequest(), userId);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.items()).hasSize(1);
            assertThat(result.totalPrice()).isEqualTo(36000L);  // 18000 * 2
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 가게")
        void 존재하지_않는_가게_예외발생() {
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
        void 존재하지_않는_배송지_예외발생() {
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
        @DisplayName("실패 - 존재하지 않는 메뉴 (삭제 또는 숨김 처리된 메뉴 포함)")
        void 존재하지_않는_메뉴_예외발생() {
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
        @DisplayName("실패 - 해당 가게 소속이 아닌 메뉴 주문 불가")
        void 다른_가게_메뉴_주문_예외발생() {
            // given
            MenuEntity mockMenu = mock(MenuEntity.class);
            given(mockMenu.getStoreId()).willReturn(UUID.randomUUID()); // 다른 가게 ID

            StoreEntity mockStore = mock(StoreEntity.class);
            given(mockStore.getStoreId()).willReturn(storeId);

            given(storeRepository.findActiveById(storeId))
                    .willReturn(Optional.of(mockStore));
            given(addressRepository.findActiveById(addressId))
                    .willReturn(Optional.of(mock(AddressEntity.class)));
            given(menuRepository.findActiveById(menuId))
                    .willReturn(Optional.of(mockMenu));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(), userId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(MenuErrorCode.MENU_NOT_IN_STORE));
        }
    }

    // ========================================================
    // 🎥 test(#9): 주문 목록 조회 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("getOrders()")
    class GetOrders {

        @Test
        @DisplayName("성공 - 전체 주문 목록 조회 (검색 조건 없음)")
        void 전체_주문_목록_조회_성공() {
            // given
            Page<OrderEntity> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchOrders(eq(userId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, null, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).storeName()).isEqualTo("BBQ 광화문점");
            assertThat(result.getContent().get(0).totalPrice()).isEqualTo(38000L);
            assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("성공 - 상태 조건으로 검색")
        void 상태_조건으로_검색_성공() {
            // given
            Page<OrderEntity> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchOrders(eq(userId), eq(OrderStatus.PENDING), isNull(), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, OrderStatus.PENDING, null, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("성공 - 가게명 조건으로 검색")
        void 가게명_조건으로_검색_성공() {
            // given
            Page<OrderEntity> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchOrders(eq(userId), isNull(), eq("BBQ"), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, "BBQ", PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).storeName()).isEqualTo("BBQ 광화문점");
        }

        @Test
        @DisplayName("성공 - 상태 + 가게명 동시 검색")
        void 상태_가게명_동시_검색_성공() {
            // given
            Page<OrderEntity> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchOrders(eq(userId), eq(OrderStatus.PENDING), eq("BBQ"), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, OrderStatus.PENDING, "BBQ", PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getContent().get(0).storeName()).isEqualTo("BBQ 광화문점");
        }

        @Test
        @DisplayName("성공 - 주문 없을 때 빈 목록 반환")
        void 주문_없을때_빈_목록_반환() {
            // given
            Page<OrderEntity> emptyPage = new PageImpl<>(List.of());
            given(orderRepository.searchOrders(eq(userId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, null, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("실패 - soft delete된 주문은 목록에 포함되지 않음")
        void soft_delete된_주문_목록_미포함() {
            // given
            // soft delete된 주문은 Repository 쿼리에서 deletedAt IS NULL 조건으로 제외됨
            Page<OrderEntity> emptyPage = new PageImpl<>(List.of());
            given(orderRepository.searchOrders(eq(userId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, null, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
            verify(orderRepository).searchOrders(eq(userId), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 주문은 조회되지 않음")
        void 다른_사용자_주문_조회_불가() {
            // given
            UUID otherUserId = UUID.randomUUID();
            Page<OrderEntity> emptyPage = new PageImpl<>(List.of());
            given(orderRepository.searchOrders(eq(otherUserId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    otherUserId, null, null, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
            verify(orderRepository, never()).searchOrders(eq(userId), isNull(), isNull(), any(Pageable.class));
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
        void 주문_단건_조회_성공() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

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
        void 주문_없음_예외발생() {
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
        void 요청사항_수정_성공() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            // when
            UpdateOrderResponse result = orderService.updateOrder(
                    orderId, new UpdateOrderRequest("수정된 요청사항"));

            // then
            assertThat(result.note()).isEqualTo("수정된 요청사항");
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문")
        void 존재하지_않는_주문_예외발생() {
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
        void PENDING_아닌_상태에서_수정_예외발생() throws Exception {
            // given
            OrderEntity order = pendingOrder();
            order.changeStatus(OrderStatus.ACCEPTED);
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
        void PENDING에서_ACCEPTED_상태전이_성공() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            // when
            UpdateOrderStatusResponse result = orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.ACCEPTED));

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.ACCEPTED);
        }

        @Test
        @DisplayName("성공 - ACCEPTED → COOKING 상태 전이")
        void ACCEPTED에서_COOKING_상태전이_성공() {
            // given
            OrderEntity order = pendingOrder();
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
        void 존재하지_않는_주문_예외발생() {
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
        void 허용되지_않는_상태전이_예외발생() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.COMPLETED)))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.INVALID_ORDER_STATUS));
        }

        @Test
        @DisplayName("실패 - 역방향 상태 전이 (ACCEPTED → PENDING)")
        void 역방향_상태전이_예외발생() {
            // given
            OrderEntity order = pendingOrder();
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

    // ========================================================
    // 🎥 test(#9): 주문 취소 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        @Test
        @DisplayName("성공 - 5분 이내 취소 (사유 있음)")
        void 주문후_5분_이내_취소_사유있음_성공() throws Exception {
            // given
            OrderEntity order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(3));
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when
            CancelOrderResponse result = orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심"));

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("성공 - 5분 이내 취소 (사유 없음)")
        void 주문후_5분_이내_취소_사유없음_성공() throws Exception {
            // given
            OrderEntity order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(3));
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when
            CancelOrderResponse result = orderService.cancelOrder(orderId, null);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("실패 - 5분 초과 취소")
        void 주문후_5분_초과_취소_예외발생() throws Exception {
            // given
            OrderEntity order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(6));
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심")))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.CANCEL_TIME_EXCEEDED));
        }

        @Test
        @DisplayName("실패 - PENDING이 아닌 상태에서 취소 시도")
        void PENDING_아닌_상태에서_취소_예외발생() throws Exception {
            // given
            OrderEntity order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(1));
            order.changeStatus(OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심")))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED));
        }

        @Test
        @DisplayName("실패 - 5분 이내지만 PENDING이 아닌 상태")
        void 주문후_5분_이내지만_PENDING_아닌_상태_예외발생() throws Exception {
            // given
            OrderEntity order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(3));
            order.changeStatus(OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심")))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문")
        void 존재하지_않는_주문_예외발생() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심")))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }
    }

    // ========================================================
    // 🎥 test(#9): 주문 거절 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("rejectOrder()")
    class RejectOrder {

        @Test
        @DisplayName("성공 - 주문 거절 (사유 있음)")
        void 주문_거절_사유있음_성공() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            // when
            RejectOrderResponse result = orderService.rejectOrder(
                    orderId, new RejectOrderRequest("재료 소진"));

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.REJECTED);
            assertThat(result.rejectReason()).isEqualTo("재료 소진");
        }

        @Test
        @DisplayName("성공 - 주문 거절 (사유 없음)")
        void 주문_거절_사유없음_성공() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            // when
            RejectOrderResponse result = orderService.rejectOrder(orderId, null);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.REJECTED);
            assertThat(result.rejectReason()).isNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문")
        void 존재하지_않는_주문_예외발생() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.rejectOrder(
                    orderId, new RejectOrderRequest("재료 소진")))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - PENDING이 아닌 상태에서 거절 시도")
        void PENDING_아닌_상태에서_거절_예외발생() {
            // given
            OrderEntity order = pendingOrder();
            order.changeStatus(OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.rejectOrder(
                    orderId, new RejectOrderRequest("재료 소진")))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_REJECT_NOT_ALLOWED));
        }
    }

    // ========================================================
    // 🎥 test(#9): 주문 삭제 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("deleteOrder()")
    class DeleteOrder {

        @Test
        @DisplayName("성공 - 주문 soft delete")
        void 주문_soft_delete_성공() {
            // given
            OrderEntity order = pendingOrder();
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when
            orderService.deleteOrder(orderId);

            // then
            assertThat(order.isDeleted()).isTrue();
            assertThat(order.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문 (이미 삭제된 주문 포함)")
        void 존재하지_않는_주문_예외발생() {
            // given
            // 존재하지 않는 주문 또는 이미 soft delete된 주문 모두
            // findActiveById에서 Optional.empty() 반환
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.deleteOrder(orderId))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }
    }
}