package com.sparta.todayeats.order.service;

import com.sparta.todayeats.address.domain.entity.AddressEntity;
import com.sparta.todayeats.address.domain.repository.AddressRepository;
import com.sparta.todayeats.global.exception.*;
import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import com.sparta.todayeats.menu.domain.repository.MenuRepository;
import com.sparta.todayeats.order.dto.request.*;
import com.sparta.todayeats.order.dto.response.*;
import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.order.entity.OrderType;
import com.sparta.todayeats.order.repository.OrderRepository;
import com.sparta.todayeats.store.entity.Store;
import com.sparta.todayeats.store.repository.StoreRepository;
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

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.*;

@DisplayName("OrderServiceV1 테스트")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();
    private final UUID menuId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    // ─── 테스트 픽스처 ────────────────────────────────────────────────────
    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private AddressRepository addressRepository;

    private CreateOrderRequest createOrderRequest() {
        return new CreateOrderRequest(
                storeId,
                addressId,
                OrderType.ONLINE,
                "문 앞에 놔주세요",
                List.of(new CreateOrderRequest.OrderItemRequest(menuId, 2))
        );
    }

    private Order pendingOrder() {
        return Order.builder()
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

    private void setCreatedAt(Order order, LocalDateTime time) throws Exception {
        Field field = order.getClass().getSuperclass().getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(order, time);
    }

    private void setStatus(Order order, OrderStatus status) throws Exception {
        Field field = order.getClass().getDeclaredField("status");
        field.setAccessible(true);
        field.set(order, status);
    }

    private void setRejectReason(Order order, String rejectReason) throws Exception {
        Field field = order.getClass().getDeclaredField("rejectReason");
        field.setAccessible(true);
        field.set(order, rejectReason);
    }

    // ========================================================
    // 🎥 test(#38): 주문 생성 단위 테스트
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

            Store mockStore = mock(Store.class);
            given(mockStore.getId()).willReturn(storeId);
            given(storeRepository.findById(storeId))
                    .willReturn(Optional.of(mockStore));

            AddressEntity mockAddress = mock(AddressEntity.class);
            given(mockAddress.getUserId()).willReturn(userId);
            given(addressRepository.findActiveById(addressId)).willReturn(Optional.of(mockAddress));

            given(menuRepository.findActiveById(menuId))
                    .willReturn(Optional.of(mockMenu));
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // when
            CreateOrderResponse result = orderService.createOrder(createOrderRequest(), userId, UserRoleEnum.CUSTOMER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.items()).hasSize(1);
            assertThat(result.totalPrice()).isEqualTo(36000L);  // 18000 * 2
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 가게")
        void 존재하지_않는_가게_예외발생() {
            // given
            given(storeRepository.findById(storeId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(StoreErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 배송지")
        void 존재하지_않는_배송지_예외발생() {
            // given
            given(storeRepository.findById(storeId))
                    .willReturn(Optional.of(mock(Store.class)));
            given(addressRepository.findActiveById(addressId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AddressErrorCode.ADDRESS_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 메뉴 (삭제 또는 숨김 처리된 메뉴 포함)")
        void 존재하지_않는_메뉴_예외발생() {
            // given
            given(storeRepository.findById(storeId))
                    .willReturn(Optional.of(mock(Store.class)));

            AddressEntity mockAddress = mock(AddressEntity.class);
            given(mockAddress.getUserId()).willReturn(userId);
            given(addressRepository.findActiveById(addressId))
                    .willReturn(Optional.of(mockAddress));

            given(menuRepository.findActiveById(menuId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(), userId, UserRoleEnum.CUSTOMER))
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

            Store mockStore = mock(Store.class);
            given(mockStore.getId()).willReturn(storeId);

            given(storeRepository.findById(storeId))
                    .willReturn(Optional.of(mockStore));

            AddressEntity mockAddress = mock(AddressEntity.class);
            given(mockAddress.getUserId()).willReturn(userId);
            given(addressRepository.findActiveById(addressId))
                    .willReturn(Optional.of(mockAddress));

            given(menuRepository.findActiveById(menuId))
                    .willReturn(Optional.of(mockMenu));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(MenuErrorCode.MENU_NOT_IN_STORE));
        }

        @Test
        @DisplayName("실패 - 숨김 처리된 가게로 주문 생성 시도")
        void 숨김_가게_주문생성_예외발생() {
            // given
            Store mockStore = mock(Store.class);
            given(mockStore.getIsHidden()).willReturn(true);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(StoreErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 아닌 역할로 주문 생성 시도")
        void CUSTOMER_아닌_역할_주문생성_예외발생() {
            // when & then
            assertThatThrownBy(() -> orderService.createOrder(
                    createOrderRequest(), userId, UserRoleEnum.OWNER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("실패 - 타인 배송지로 주문 생성 시도")
        void 타인_배송지_주문생성_예외발생() {
            //given
            Store mockStore = mock(Store.class);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore));
            AddressEntity mockAddress = mock(AddressEntity.class);
            given(mockAddress.getUserId()).willReturn(UUID.randomUUID()); // 다른 유저 ID
            given(addressRepository.findActiveById(addressId)).willReturn(Optional.of(mockAddress));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(
                    createOrderRequest(), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }
    }

    // ========================================================
    // 🎥 test(#38): 주문 목록 조회 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("getOrders()")
    class GetOrders {

        @Test
        @DisplayName("성공 - 전체 주문 목록 조회 (검색 조건 없음)")
        void 전체_주문_목록_조회_성공() {
            // given
            Page<Order> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchOrders(eq(userId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, null, PageRequest.of(0, 10), UserRoleEnum.CUSTOMER);

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
            Page<Order> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchOrders(eq(userId), eq(OrderStatus.PENDING), isNull(), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, OrderStatus.PENDING, null, PageRequest.of(0, 10), UserRoleEnum.CUSTOMER);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("성공 - 가게명 조건으로 검색")
        void 가게명_조건으로_검색_성공() {
            // given
            Page<Order> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchOrders(eq(userId), isNull(), eq("BBQ"), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, "BBQ", PageRequest.of(0, 10), UserRoleEnum.CUSTOMER);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).storeName()).isEqualTo("BBQ 광화문점");
        }

        @Test
        @DisplayName("성공 - 상태 + 가게명 동시 검색")
        void 상태_가게명_동시_검색_성공() {
            // given
            Page<Order> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchOrders(eq(userId), eq(OrderStatus.PENDING), eq("BBQ"), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, OrderStatus.PENDING, "BBQ", PageRequest.of(0, 10), UserRoleEnum.CUSTOMER);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getContent().get(0).storeName()).isEqualTo("BBQ 광화문점");
        }

        @Test
        @DisplayName("성공 - 주문 없을 때 빈 목록 반환")
        void 주문_없을때_빈_목록_반환() {
            // given
            Page<Order> emptyPage = new PageImpl<>(List.of());
            given(orderRepository.searchOrders(eq(userId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, null, PageRequest.of(0, 10), UserRoleEnum.CUSTOMER);

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("성공 - Repository가 빈 결과 반환 시 빈 목록 응답")
        void soft_delete된_주문_목록_미포함() {
            // given
            // soft delete된 주문은 Repository 쿼리에서 deletedAt IS NULL 조건으로 제외됨
            Page<Order> emptyPage = new PageImpl<>(List.of());
            given(orderRepository.searchOrders(eq(userId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, null, PageRequest.of(0, 10), UserRoleEnum.CUSTOMER);

            // then
            assertThat(result.getContent()).isEmpty();
            verify(orderRepository).searchOrders(eq(userId), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("성공 - 다른 userId로 조회 시 해당 userId의 결과만 반환")
        void 다른_사용자_주문_조회_불가() {
            // given
            UUID otherUserId = UUID.randomUUID();
            Page<Order> emptyPage = new PageImpl<>(List.of());
            given(orderRepository.searchOrders(eq(otherUserId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    otherUserId, null, null, PageRequest.of(0, 10), UserRoleEnum.CUSTOMER);

            // then
            assertThat(result.getContent()).isEmpty();
            verify(orderRepository, never()).searchOrders(eq(userId), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("성공 - OWNER는 본인 가게 주문만 조회")
        void OWNER_본인_가게_주문_조회_성공() {
            // given
            Page<Order> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchOrdersByStoreOwner(eq(userId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, null, PageRequest.of(0, 10), UserRoleEnum.OWNER);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(orderRepository).searchOrdersByStoreOwner(eq(userId), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("성공 - MANAGER는 전체 주문 조회 (soft delete 제외)")
        void MANAGER_전체_주문_조회_성공() {
            // given
            Page<Order> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchAllOrders(isNull(), isNull(), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, null, PageRequest.of(0, 10), UserRoleEnum.MANAGER);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(orderRepository).searchAllOrders(isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("성공 - MASTER는 삭제된 주문 포함 전체 조회")
        void MASTER_삭제포함_전체_주문_조회_성공() {
            // given
            Page<Order> page = new PageImpl<>(List.of(pendingOrder()));
            given(orderRepository.searchAllOrdersIncludeDeleted(isNull(), isNull(), any(Pageable.class)))
                    .willReturn(page);

            // when
            Page<OrderSummaryResponse> result = orderService.getOrders(
                    userId, null, null, PageRequest.of(0, 10), UserRoleEnum.MASTER);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(orderRepository).searchAllOrdersIncludeDeleted(isNull(), isNull(), any(Pageable.class));
        }
    }

    // ========================================================
    // 🎥 test(#38): 주문 단건 조회 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("getOrder()")
    class GetOrder {

        @Test
        @DisplayName("성공 - CUSTOMER 본인 주문 조회")
        void CUSTOMER_본인_주문_조회_성공() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            // when
            OrderDetailResponse result = orderService.getOrder(orderId, userId, UserRoleEnum.CUSTOMER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.store().storeName()).isEqualTo("BBQ 광화문점");
            assertThat(result.totalPrice()).isEqualTo(38000L);
        }

        @Test
        @DisplayName("성공 - OWNER 본인 가게 주문 조회")
        void OWNER_본인_가게_주문_조회_성공() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            User mockOwner = mock(User.class);
            given(mockOwner.getUserId()).willReturn(userId);
            Store mockStore = mock(Store.class);
            given(mockStore.getOwner()).willReturn(mockOwner);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore));

            // when
            OrderDetailResponse result = orderService.getOrder(orderId, userId, UserRoleEnum.OWNER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("성공 - MANAGER 전체 주문 조회")
        void MANAGER_주문_조회_성공() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            // when
            OrderDetailResponse result = orderService.getOrder(orderId, userId, UserRoleEnum.MANAGER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("성공 - MASTER 전체 주문 조회")
        void MASTER_주문_조회_성공() {
            // given
            given(orderRepository.findById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            // when
            OrderDetailResponse result = orderService.getOrder(orderId, userId, UserRoleEnum.MASTER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
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
            assertThatThrownBy(() -> orderService.getOrder(orderId, userId, UserRoleEnum.MANAGER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 타인 주문 조회 시도")
        void CUSTOMER_타인_주문_조회_예외발생() {
            // given
            Order order = pendingOrder(); // customerId = userId
            UUID otherUserId = UUID.randomUUID();
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.getOrder(orderId, otherUserId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("실패 - OWNER가 타인 가게 주문 조회 시도")
        void OWNER_타인_가게_주문_조회_예외발생() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            User mockOwner = mock(User.class);
            given(mockOwner.getUserId()).willReturn(UUID.randomUUID()); // 다른 owner
            Store mockStore = mock(Store.class);
            given(mockStore.getOwner()).willReturn(mockOwner);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore));

            // when & then
            assertThatThrownBy(() -> orderService.getOrder(orderId, userId, UserRoleEnum.OWNER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }
    }

    // ========================================================
    // 🎥 test(#38): 주문 수정 단위 테스트
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
                    orderId, new UpdateOrderRequest("수정된 요청사항"), userId, UserRoleEnum.CUSTOMER);

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
                    orderId, new UpdateOrderRequest("수정된 요청사항"), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - PENDING이 아닌 상태에서 수정 시도")
        void PENDING_아닌_상태에서_수정_예외발생() throws Exception {
            // given
            Order order = pendingOrder();
            setStatus(order, OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.updateOrder(
                    orderId, new UpdateOrderRequest("수정된 요청사항"), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_UPDATE_NOT_ALLOWED));
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 아닌 역할로 수정 시도")
        void CUSTOMER_아닌_역할_수정_예외발생() {
            // given

            // when & then
            assertThatThrownBy(() -> orderService.updateOrder(
                    orderId, new UpdateOrderRequest("수정된 요청사항"), userId, UserRoleEnum.OWNER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 타인 주문 수정 시도")
        void CUSTOMER_타인_주문_수정_예외발생() {
            // given
            UUID otherUserId = UUID.randomUUID();
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder())); // customerId = userId

            // when & then
            assertThatThrownBy(() -> orderService.updateOrder(
                    orderId, new UpdateOrderRequest("수정된 요청사항"), otherUserId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }

    }

    // ========================================================
    // 🎥 test(#38): 주문 상태 변경 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("updateOrderStatus()")
    class UpdateOrderStatus {

        @Test
        @DisplayName("성공 - PENDING → ACCEPTED 상태 전이")
        void PENDING에서_ACCEPTED_상태전이_성공() throws Exception {
            // given
            Order acceptedOrder = pendingOrder();
            setStatus(acceptedOrder, OrderStatus.ACCEPTED);  // DB 업데이트 후 재조회 결과 시뮬레이션

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()))   // 첫 번째 조회 (검증용)
                    .willReturn(Optional.of(acceptedOrder));   // 두 번째 조회 (재조회용)
            given(orderRepository.updateStatusConditionally(orderId, OrderStatus.PENDING, OrderStatus.ACCEPTED, userId))
                    .willReturn(1);

            // when
            UpdateOrderStatusResponse result = orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.ACCEPTED), userId, UserRoleEnum.MANAGER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.ACCEPTED);
        }

        @Test
        @DisplayName("성공 - ACCEPTED → COOKING 상태 전이")
        void ACCEPTED에서_COOKING_상태전이_성공() throws Exception {
            // given
            Order acceptedOrder = pendingOrder();
            setStatus(acceptedOrder, OrderStatus.ACCEPTED);
            Order cookingOrder = pendingOrder();
            setStatus(cookingOrder, OrderStatus.COOKING);

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(acceptedOrder))   // 첫 번째 조회 (검증용)
                    .willReturn(Optional.of(cookingOrder));    // 두 번째 조회 (재조회용)
            given(orderRepository.updateStatusConditionally(orderId, OrderStatus.ACCEPTED, OrderStatus.COOKING, userId))
                    .willReturn(1);

            // when
            UpdateOrderStatusResponse result = orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.COOKING), userId, UserRoleEnum.MANAGER);

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
                    orderId, new UpdateOrderStatusRequest(OrderStatus.ACCEPTED), userId, UserRoleEnum.MANAGER))
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
                    orderId, new UpdateOrderStatusRequest(OrderStatus.COMPLETED), userId, UserRoleEnum.MANAGER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.INVALID_ORDER_STATUS));
        }

        @Test
        @DisplayName("실패 - 역방향 상태 전이 (ACCEPTED → PENDING)")
        void 역방향_상태전이_예외발생() throws Exception {
            // given
            Order order = pendingOrder();
            setStatus(order, OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.PENDING), userId, UserRoleEnum.MANAGER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.INVALID_ORDER_STATUS));
        }

        @Test
        @DisplayName("실패 - 동시 요청으로 인한 충돌")
        void 동시_요청_충돌_예외발생() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));
            given(orderRepository.updateStatusConditionally(orderId, OrderStatus.PENDING, OrderStatus.ACCEPTED, userId))
                    .willReturn(0);  // 다른 요청이 이미 상태 변경 → 0건 업데이트

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.ACCEPTED), userId, UserRoleEnum.MANAGER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_CONFLICT));
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 상태 변경 시도")
        void CUSTOMER_상태변경_예외발생() {
            // given

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.ACCEPTED), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("실패 - OWNER가 타인 가게 주문 상태 변경 시도")
        void OWNER_타인_가게_주문_상태변경_예외발생() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));

            User mockOwner = mock(User.class);
            given(mockOwner.getUserId()).willReturn(UUID.randomUUID()); // 다른 owner
            Store mockStore = mock(Store.class);
            given(mockStore.getOwner()).willReturn(mockOwner);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore));

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.ACCEPTED), userId, UserRoleEnum.OWNER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("성공 - OWNER 본인 가게 주문 상태 변경")
        void OWNER_본인_가게_주문_상태변경_성공() throws Exception {
            // given
            Order acceptedOrder = pendingOrder();
            setStatus(acceptedOrder, OrderStatus.ACCEPTED);

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()))
                    .willReturn(Optional.of(acceptedOrder));

            User mockOwner = mock(User.class);
            given(mockOwner.getUserId()).willReturn(userId);
            Store mockStore = mock(Store.class);
            given(mockStore.getOwner()).willReturn(mockOwner);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore));
            given(orderRepository.updateStatusConditionally(orderId, OrderStatus.PENDING, OrderStatus.ACCEPTED, userId))
                    .willReturn(1);

            // when
            UpdateOrderStatusResponse result = orderService.updateOrderStatus(
                    orderId, new UpdateOrderStatusRequest(OrderStatus.ACCEPTED), userId, UserRoleEnum.OWNER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.ACCEPTED);
        }
    }

    // ========================================================
    // 🎥 test(#38): 주문 취소 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        @Test
        @DisplayName("성공 - 5분 이내 취소 (사유 있음)")
        void 주문후_5분_이내_취소_사유있음_성공() throws Exception {
            // given
            Order order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(3));
            Order canceledOrder = pendingOrder();
            setStatus(canceledOrder, OrderStatus.CANCELED);

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order))
                    .willReturn(Optional.of(canceledOrder));
            given(orderRepository.cancelConditionally(eq(orderId), eq("단순 변심"), eq(OrderStatus.PENDING.name()), eq(OrderStatus.CANCELED.name()), eq(userId)))
                    .willReturn(1);

            // when
            CancelOrderResponse result = orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심"), userId, UserRoleEnum.CUSTOMER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("성공 - 5분 이내 취소 (사유 없음)")
        void 주문후_5분_이내_취소_사유없음_성공() throws Exception {
            // given
            Order order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(3));
            Order canceledOrder = pendingOrder();
            setStatus(canceledOrder, OrderStatus.CANCELED);

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order))
                    .willReturn(Optional.of(canceledOrder));
            given(orderRepository.cancelConditionally(eq(orderId), isNull(), eq(OrderStatus.PENDING.name()), eq(OrderStatus.CANCELED.name()), eq(userId)))
                    .willReturn(1);

            // when
            CancelOrderResponse result = orderService.cancelOrder(orderId, null, userId, UserRoleEnum.CUSTOMER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("실패 - 5분 초과 취소")
        void 주문후_5분_초과_취소_예외발생() throws Exception {
            // given
            Order order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(6));
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심"), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.CANCEL_TIME_EXCEEDED));
        }

        @Test
        @DisplayName("실패 - PENDING이 아닌 상태에서 취소 시도")
        void PENDING_아닌_상태에서_취소_예외발생() throws Exception {
            // given
            Order order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(1));
            setStatus(order, OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심"), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED));
        }

        @Test
        @DisplayName("실패 - 5분 이내지만 PENDING이 아닌 상태")
        void 주문후_5분_이내지만_PENDING_아닌_상태_예외발생() throws Exception {
            // given
            Order order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(3));
            setStatus(order, OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심"), userId, UserRoleEnum.CUSTOMER))
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
                    orderId, new CancelOrderRequest("단순 변심"), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 동시 요청으로 인한 충돌 (다른 요청이 먼저 상태 변경)")
        void 동시_요청_충돌_예외발생() throws Exception {
            // given
            Order order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(3));

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));             // 첫 번째 조회 (검증용)
            given(orderRepository.cancelConditionally(eq(orderId), any(), eq(OrderStatus.PENDING.name()), eq(OrderStatus.CANCELED.name()), eq(userId)))
                    .willReturn(0);

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심"), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_CONFLICT));
        }

        @Test
        @DisplayName("실패 - OWNER가 취소 시도")
        void OWNER_취소_예외발생() {
            // given

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심"), userId, UserRoleEnum.OWNER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("실패 - MANAGER가 취소 시도")
        void MANAGER_취소_예외발생() {
            // given

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심"), userId, UserRoleEnum.MANAGER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 타인 주문 취소 시도")
        void CUSTOMER_타인_주문_취소_예외발생() {
            // given
            UUID otherUserId = UUID.randomUUID();
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder())); // customerId = userId

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심"), otherUserId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("성공 - MASTER가 취소")
        void MASTER_취소_성공() throws Exception {
            // given
            Order order = pendingOrder();
            setCreatedAt(order, LocalDateTime.now().minusMinutes(3));
            Order canceledOrder = pendingOrder();
            setStatus(canceledOrder, OrderStatus.CANCELED);

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order))
                    .willReturn(Optional.of(canceledOrder));
            given(orderRepository.cancelConditionally(eq(orderId), eq("단순 변심"), eq(OrderStatus.PENDING.name()), eq(OrderStatus.CANCELED.name()), eq(userId)))
                    .willReturn(1);

            // when
            CancelOrderResponse result = orderService.cancelOrder(
                    orderId, new CancelOrderRequest("단순 변심"), userId, UserRoleEnum.MASTER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.CANCELED);
        }
    }

    // ========================================================
    // 🎥 test(#38): 주문 거절 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("rejectOrder()")
    class RejectOrder {

        @Test
        @DisplayName("성공 - 주문 거절 (사유 있음)")
        void 주문_거절_사유있음_성공() throws Exception {
            // given
            Order rejectedOrder = pendingOrder();
            setStatus(rejectedOrder, OrderStatus.REJECTED);
            setRejectReason(rejectedOrder, "재료 소진");

            User mockOwner = mock(User.class);
            given(mockOwner.getUserId()).willReturn(userId);
            Store mockStore = mock(Store.class);
            given(mockStore.getOwner()).willReturn(mockOwner);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore));

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()))
                    .willReturn(Optional.of(rejectedOrder));
            given(orderRepository.rejectConditionally(eq(orderId), eq("재료 소진"), eq(OrderStatus.PENDING), eq(OrderStatus.REJECTED), eq(userId))).willReturn(1);

            // when
            RejectOrderResponse result = orderService.rejectOrder(orderId, new RejectOrderRequest("재료 소진"), userId, UserRoleEnum.OWNER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.REJECTED);
            assertThat(result.rejectReason()).isEqualTo("재료 소진");
        }

        @Test
        @DisplayName("성공 - 주문 거절 (사유 없음)")
        void 주문_거절_사유없음_성공() throws Exception {
            // given
            Order rejectedOrder = pendingOrder();
            setStatus(rejectedOrder, OrderStatus.REJECTED);

            User mockOwner = mock(User.class);
            given(mockOwner.getUserId()).willReturn(userId);
            Store mockStore = mock(Store.class);
            given(mockStore.getOwner()).willReturn(mockOwner);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore));

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()))
                    .willReturn(Optional.of(rejectedOrder));
            given(orderRepository.rejectConditionally(eq(orderId), isNull(), eq(OrderStatus.PENDING), eq(OrderStatus.REJECTED), eq(userId))).willReturn(1);

            // when
            RejectOrderResponse result = orderService.rejectOrder(orderId, null, userId, UserRoleEnum.OWNER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.REJECTED);
            assertThat(result.rejectReason()).isNull();
        }

        @Test
        @DisplayName("성공 - MASTER가 주문 거절")
        void MASTER_주문_거절_성공() throws Exception {
            // given
            Order rejectedOrder = pendingOrder();
            setStatus(rejectedOrder, OrderStatus.REJECTED);
            setRejectReason(rejectedOrder, "운영자 거절");

            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()))
                    .willReturn(Optional.of(rejectedOrder));
            given(orderRepository.rejectConditionally(eq(orderId), eq("운영자 거절"), eq(OrderStatus.PENDING), eq(OrderStatus.REJECTED), eq(userId)))
                    .willReturn(1);

            // when
            RejectOrderResponse result = orderService.rejectOrder(orderId, new RejectOrderRequest("운영자 거절"), userId, UserRoleEnum.MASTER);

            // then
            assertThat(result.status()).isEqualTo(OrderStatus.REJECTED);
            assertThat(result.rejectReason()).isEqualTo("운영자 거절");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문")
        void 존재하지_않는_주문_예외발생() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.rejectOrder(orderId, new RejectOrderRequest("재료 소진"), userId, UserRoleEnum.MANAGER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - PENDING이 아닌 상태에서 거절 시도")
        void PENDING_아닌_상태에서_거절_예외발생() throws Exception {
            // given
            Order order = pendingOrder();
            setStatus(order, OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.rejectOrder(orderId, new RejectOrderRequest("재료 소진"), userId, UserRoleEnum.MANAGER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_REJECT_NOT_ALLOWED));
        }

        @Test
        @DisplayName("실패 - 동시 요청으로 인한 충돌")
        void 동시_요청_충돌_예외발생() {
            // given
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(pendingOrder()));
            given(orderRepository.rejectConditionally(eq(orderId), any(), eq(OrderStatus.PENDING), eq(OrderStatus.REJECTED), eq(userId)))
                    .willReturn(0);
            // 다른 요청이 이미 상태 변경 → 0건 업데이트

            // when & then
            assertThatThrownBy(() -> orderService.rejectOrder(orderId, new RejectOrderRequest("재료 소진"), userId, UserRoleEnum.MANAGER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_CONFLICT));
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 거절 시도")
        void CUSTOMER_거절_예외발생() {
            // when & then
            assertThatThrownBy(() -> orderService.rejectOrder(
                    orderId, new RejectOrderRequest("재료 소진"), userId, UserRoleEnum.CUSTOMER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("실패 - OWNER가 타인 가게 주문 거절 시도")
        void OWNER_타인_가게_주문_거절_예외발생() {
            //given
            given(orderRepository.findActiveById(orderId)).willReturn(Optional.of(pendingOrder()));
            User mockOwner = mock(User.class);
            given(mockOwner.getUserId()).willReturn(UUID.randomUUID());
            Store mockStore = mock(Store.class);
            given(mockStore.getOwner()).willReturn(mockOwner);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore));

            // when & then
            assertThatThrownBy(() -> orderService.rejectOrder(
                    orderId, new RejectOrderRequest("재료 소진"), userId, UserRoleEnum.OWNER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }
    }

    // ========================================================
    // 🎥 test(#38): 주문 삭제 단위 테스트
    // ========================================================

    @Nested
    @DisplayName("deleteOrder()")
    class DeleteOrder {

        @Test
        @DisplayName("성공 - 주문 soft delete")
        void 주문_soft_delete_성공() {
            // given
            Order order = pendingOrder();
            given(orderRepository.findActiveById(orderId))
                    .willReturn(Optional.of(order));

            // when
            orderService.deleteOrder(orderId, userId, UserRoleEnum.MASTER);

            // then
            assertThat(order.isDeleted()).isTrue();
            assertThat(order.getDeletedAt()).isNotNull();
            assertThat(order.getDeletedBy()).isEqualTo(userId);
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
            assertThatThrownBy(() -> orderService.deleteOrder(orderId, userId, UserRoleEnum.MASTER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - MASTER가 아닌 역할로 삭제 시도")
        void MASTER_아닌_역할_삭제_예외발생() {
            // when & then
            assertThatThrownBy(() -> orderService.deleteOrder(orderId, userId, UserRoleEnum.MANAGER))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(CommonErrorCode.FORBIDDEN));
        }
    }
}