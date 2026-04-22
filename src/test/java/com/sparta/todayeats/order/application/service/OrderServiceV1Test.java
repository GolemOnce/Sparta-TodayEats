package com.sparta.todayeats.order.application.service;

import com.sparta.todayeats.address.domain.entity.AddressEntity;
import com.sparta.todayeats.address.domain.repository.AddressRepository;
import com.sparta.todayeats.global.exception.AddressErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.MenuErrorCode;
import com.sparta.todayeats.global.exception.StoreErrorCode;
import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import com.sparta.todayeats.menu.domain.repository.MenuRepository;
import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderStatus;
import com.sparta.todayeats.order.domain.entity.OrderType;
import com.sparta.todayeats.order.domain.repository.OrderRepository;
import com.sparta.todayeats.order.presentation.dto.request.CreateOrderRequest;
import com.sparta.todayeats.order.presentation.dto.response.CreateOrderResponse;
import com.sparta.todayeats.store.domain.entity.StoreEntity;
import com.sparta.todayeats.store.domain.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    // createOrder() 테스트
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
    }
}