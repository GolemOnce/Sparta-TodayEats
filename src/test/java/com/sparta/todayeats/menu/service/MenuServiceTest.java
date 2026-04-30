package com.sparta.todayeats.menu.service;

import com.sparta.todayeats.category.entity.Category;
import com.sparta.todayeats.category.repository.CategoryRepository;
import com.sparta.todayeats.menu.entity.Menu;
import com.sparta.todayeats.menu.repository.MenuRepository;
import com.sparta.todayeats.menu.dto.request.MenuCreateRequest;
import com.sparta.todayeats.menu.dto.request.MenuUpdateRequest;
import com.sparta.todayeats.store.entity.Store;
import com.sparta.todayeats.store.repository.StoreRepository;
import com.sparta.todayeats.user.entity.User;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("MenuService 테스트")
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {
    @InjectMocks
    private MenuService menuService;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StoreRepository storeRepository;

    @Nested
    @DisplayName("createMenu()")
    class CreateMenu {

            @Test
            @DisplayName("성공 - 메뉴 생성")
            void success() {
                UUID storeId = UUID.randomUUID();
                UUID categoryId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                MenuCreateRequest request = new MenuCreateRequest(
                        "김치찌개",
                        9000L,
                        "돼지고기 김치찌개",
                        "image-url",
                        categoryId,
                        false
                );

                Category category = mock(Category.class);
                Store store = mock(Store.class);
                User owner = mock(User.class);

                given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
                given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
                given(store.getOwner()).willReturn(owner);
                given(owner.getUserId()).willReturn(userId);
                given(menuRepository.save(any(Menu.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                Menu result = menuService.createMenu(storeId, request, userId);

                assertThat(result.getName()).isEqualTo("김치찌개");
                assertThat(result.getPrice()).isEqualTo(9000L);
                assertThat(result.getDescription()).isEqualTo("돼지고기 김치찌개");
                assertThat(result.getImageUrl()).isEqualTo("image-url");
                assertThat(result.isHidden()).isFalse();
                assertThat(result.isSoldOut()).isFalse();
                assertThat(result.isDeleted()).isFalse();

                then(menuRepository).should().save(any(Menu.class));
            }

            @Test
            @DisplayName("실패 - 카테고리가 없을 경우")
            void fail_category_not_found() {
                // given
                UUID userId = UUID.randomUUID();
                UUID storeId = UUID.randomUUID();
                UUID categoryId = UUID.randomUUID();

                MenuCreateRequest request = new MenuCreateRequest(
                        "김치찌개",
                        9000L,
                        "돼지고기 김치찌개",
                        "image-url",
                        categoryId,
                        false
                );

                given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> menuService.createMenu(storeId, request, userId))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("카테고리 없음");
            }

        @Test
        @DisplayName("실패 - 가게가 없을 경우")
        void fail_store_not_found() {
            UUID storeId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            MenuCreateRequest request = new MenuCreateRequest(
                    "김치찌개",
                    9000L,
                    "돼지고기 김치찌개",
                    "image-url",
                    categoryId,
                    false
            );

            Category category = mock(Category.class);

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
            given(storeRepository.findById(storeId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> menuService.createMenu(storeId, request, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("가게 없음");
        }
    }

    @Nested
    @DisplayName("getMenusByStore()")
    class GetMenusByStore {

        @Test
        @DisplayName("성공 - 고객용 가게 메뉴 조회")
        void success() {
            // given
            UUID storeId = UUID.randomUUID();

            Menu menu = Menu.builder()
                    .name("김치찌개")
                    .price(9000L)
                    .build();

            String keyword = "김치";
            Pageable pageable = PageRequest.of(0, 10);

            given(menuRepository.findOrderableMenusByStoreId(storeId, keyword, pageable))
                    .willReturn(new PageImpl<>(List.of(menu)));

            // when
            Page<Menu> result = menuService.getMenusByStore(storeId, keyword, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("김치찌개");
        }
    }

    @Nested
    @DisplayName("getOwnerMenusByStore()")
    class GetOwnerMenusByStore {

        @Test
        @DisplayName("성공 - 사장님 메뉴 조회")
        void success() {
            UUID storeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Store store = mock(Store.class);
            User owner = mock(User.class);

            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            given(store.getOwner()).willReturn(owner);
            given(owner.getUserId()).willReturn(userId);

            Menu hiddenMenu = Menu.builder()
                    .name("숨김 메뉴")
                    .price(10000L)
                    .isHidden(true)
                    .build();

            String keyword = "숨김";
            Pageable pageable = PageRequest.of(0, 10);

            given(menuRepository.findOwnerMenusByStoreId(storeId, keyword, pageable))
                    .willReturn(new PageImpl<>(List.of(hiddenMenu)));

            Page<Menu> result = menuService.getOwnerMenusByStore(storeId, userId, keyword, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).isHidden()).isTrue();
            assertThat(result.getContent().get(0).isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("getMenusByCategory()")
    class GetMenusByCategory {

        @Test
        @DisplayName("성공 - 고객용 카테고리별 메뉴 조회")
        void success() {
            // given
            UUID storeId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            Menu menu = Menu.builder()
                    .name("콜라")
                    .price(2000L)
                    .build();

            String keyword = "콜라";
            Pageable pageable = PageRequest.of(0, 10);

            given(menuRepository.findVisibleMenusByStoreAndCategory(
                    storeId,
                    categoryId,
                    keyword,
                    pageable
            )).willReturn(new PageImpl<>(List.of(menu)));

            // when
            Page<Menu> result = menuService.getMenusByCategory(
                    storeId,
                    categoryId,
                    keyword,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("콜라");
        }
    }

    @Nested
    @DisplayName("updateMenu()")
    class UpdateMenu {

            @Test
            @DisplayName("성공 - 메뉴 수정")
            void success() {
                // given
                UUID menuId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                Store store = mock(Store.class);
                User owner = mock(User.class);

                given(store.getOwner()).willReturn(owner);
                given(owner.getUserId()).willReturn(userId);

                Menu menu = Menu.builder()
                        .name("김치찌개")
                        .price(9000L)
                        .description("기존 설명")
                        .imageUrl("old-image")
                        .store(store)
                        .build();

                    MenuUpdateRequest request = new MenuUpdateRequest(
                            "된장찌개",
                            8500L,
                            "수정된 설명",
                            "new-image"
                    );

                    given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

                    // when
                    menuService.updateMenu(menuId, userId, request);

                    // then
                    assertThat(menu.getName()).isEqualTo("된장찌개");
                    assertThat(menu.getPrice()).isEqualTo(8500L);
                    assertThat(menu.getDescription()).isEqualTo("수정된 설명");
                    assertThat(menu.getImageUrl()).isEqualTo("new-image");
                }

            @Test
            @DisplayName("실패 - 메뉴가 없을 경우")
            void fail_menu_not_found() {
                // given
                UUID menuId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                MenuUpdateRequest request = new MenuUpdateRequest(
                        "된장찌개",
                        8500L,
                        "수정된 설명",
                        "new-image"
                );

            given(menuRepository.findById(menuId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> menuService.updateMenu(menuId, userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("메뉴 없음");
        }

            @Test
            @DisplayName("실패 - 삭제된 메뉴 수정")
            void fail_deleted_menu() {
                // given
                UUID menuId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                Menu menu = Menu.builder()
                        .name("삭제된 메뉴")
                        .price(9000L)
                        .build();
                menu.softDelete(UUID.randomUUID());

                MenuUpdateRequest request = new MenuUpdateRequest(
                        "수정 메뉴",
                        10000L,
                        "수정 설명",
                        "image-url"
                );

                given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

                // when & then
                assertThatThrownBy(() -> menuService.updateMenu(menuId, userId, request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("삭제된 메뉴입니다.");
            }
        }
    }