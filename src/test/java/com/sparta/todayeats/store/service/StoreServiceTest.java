package com.sparta.todayeats.store.service;

import com.sparta.todayeats.area.domain.entity.Area;
import com.sparta.todayeats.area.domain.repository.AreaRepository;
import com.sparta.todayeats.category.domain.entity.Category;
import com.sparta.todayeats.category.domain.repository.CategoryRepository;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.store.dto.request.StoreCreateRequest;
import com.sparta.todayeats.store.dto.request.StoreHiddenRequest;
import com.sparta.todayeats.store.dto.request.StoreUpdateRequest;
import com.sparta.todayeats.store.dto.response.StoreCreateResponse;
import com.sparta.todayeats.store.dto.response.StoreHiddenResponse;
import com.sparta.todayeats.store.dto.response.StoreResponse;
import com.sparta.todayeats.store.entity.Store;
import com.sparta.todayeats.store.repository.StoreRepository;
import com.sparta.todayeats.user.entity.User;
import com.sparta.todayeats.user.entity.UserRoleEnum;
import com.sparta.todayeats.user.repository.UserRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    // 공통 픽스처
    private User createUser(UUID userId) {
        User user = User.builder()
                .email("owner@test.com")
                .password("Test1234!")
                .nickname("테스트사장")
                .role(UserRoleEnum.OWNER)
                .build();

        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("userId");
            field.setAccessible(true);
            field.set(user, userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return user;
    }

    private Area createArea() {
        return Area.builder()
                .id(UUID.randomUUID())
                .name("이태원")
                .city("서울특별시")
                .district("용산구")
                .isActive(true)
                .build();
    }

    private Category createCategory() {
        return Category.builder()
                .id(UUID.randomUUID())
                .name("한식")
                .build();
    }

    private Store createStore(User owner, Area area, Category category) {
        return Store.builder()
                .owner(owner)
                .area(area)
                .category(category)
                .name("맛있는 치킨")
                .address("서울특별시 용산구 이태원로 123")
                .phone("02-1234-5678")
                .build();
    }


    // 가게 생성
    @Nested
    @DisplayName("가게 생성")
    class CreateStore {

        @Test
        void 가게_생성_성공() {
            // given
            UUID userId = UUID.randomUUID();
            User owner = createUser(userId);
            Area area = createArea();
            Category category = createCategory();

            StoreCreateRequest request = new StoreCreateRequest(
                    "맛있는 치킨",
                    "서울특별시 용산구 이태원로 123",
                    "02-1234-5678",
                    area.getId(),
                    category.getId()
            );

            given(storeRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("맛있는 치킨")).willReturn(false);
            given(userRepository.findById(userId)).willReturn(Optional.of(owner));
            given(areaRepository.findById(area.getId())).willReturn(Optional.of(area));
            given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
            given(storeRepository.save(any(Store.class))).willAnswer(invocation -> {
                Store s = invocation.getArgument(0);
                return Store.builder()
                        .owner(s.getOwner())
                        .area(s.getArea())
                        .category(s.getCategory())
                        .name(s.getName())
                        .address(s.getAddress())
                        .phone(s.getPhone())
                        .build();
            });

            // when
            StoreCreateResponse result = storeService.createStore(request, userId);

            // then
            assertThat(result.getName()).isEqualTo("맛있는 치킨");
            assertThat(result.getOwnerNickname()).isEqualTo("테스트사장");
            assertThat(result.getAreaName()).isEqualTo("이태원");
            assertThat(result.getCategoryName()).isEqualTo("한식");
        }

        @Test
        void 가게_이름이_중복이면_예외발생() {
            // given
            UUID userId = UUID.randomUUID();
            UUID areaId = UUID.randomUUID();

            StoreCreateRequest request = new StoreCreateRequest(
                    "맛있는 치킨",
                    "서울특별시 용산구 이태원로 123",
                    "02-1234-5678",
                    areaId,
                    UUID.randomUUID()
            );

            // Area mock — active 상태
            Area mockArea = mock(Area.class);
            given(mockArea.getIsActive()).willReturn(true);
            given(areaRepository.findById(areaId)).willReturn(Optional.of(mockArea));

            // 이름 중복 → 예외 발생 포인트
            given(storeRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("맛있는 치킨"))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() ->
                    storeService.createStore(request, userId)
            ).isInstanceOf(BaseException.class);
        }

        @Test
        void 소유자가_존재하지_않으면_예외발생() {
            // given
            UUID userId = UUID.randomUUID();
            UUID areaId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            StoreCreateRequest request = new StoreCreateRequest(
                    "맛있는 치킨",
                    "서울특별시 용산구 이태원로 123",
                    "02-1234-5678",
                    areaId,
                    categoryId
            );

            // Area mock — active 상태로 세팅
            Area mockArea = mock(Area.class);
            given(mockArea.getIsActive()).willReturn(true);
            given(areaRepository.findById(areaId)).willReturn(Optional.of(mockArea));

            // 이름 중복 없음
            given(storeRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("맛있는 치킨"))
                    .willReturn(false);

            // User 없음 → 예외 발생 포인트
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    storeService.createStore(request, userId)
            ).isInstanceOf(BaseException.class);
        }
    }


    // 가게 목록 조회
    @Nested
    @DisplayName("가게 목록 조회")
    class GetStores {

        @Test
        void 가게_전체조회_성공() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            Store store = createStore(createUser(UUID.randomUUID()), createArea(), createCategory());

            Page<Store> page = new PageImpl<>(List.of(store), pageable, 1);

            given(storeRepository.searchStores(null, null, pageable, null, null))
                    .willReturn(page);

            // when
            PageResponse<StoreResponse> result =
                    storeService.getStores(null, null, pageable, null, null);

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        void 카테고리_이름으로_검색조회_성공() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            User owner = createUser(UUID.randomUUID());
            Area area = createArea();
            Category category = createCategory();

            Store store = createStore(owner, area, category);

            Page<Store> pageResult =
                    new PageImpl<>(List.of(store), pageable, 1);

            given(storeRepository.searchStores("한식", null, pageable, null, null))
                    .willReturn(pageResult);

            // when
            PageResponse<StoreResponse> result =
                    storeService.getStores("한식", null, pageable, null, null);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCategoryName()).isEqualTo("한식");
        }

        @Test
        void 가게이름으로_검색조회_성공() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            User owner = createUser(UUID.randomUUID());
            Area area = createArea();
            Category category = createCategory();
            Store store = createStore(owner, area, category);

            Page<Store> pageResult = new PageImpl<>(List.of(store), pageable, 1);

            given(storeRepository.searchStores(null, "치킨", pageable, null, null)).willReturn(pageResult);

            // when
            PageResponse<StoreResponse> result = storeService.getStores(null, "치킨", pageable,null,null);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("맛있는 치킨");
        }

        @Test
        void 카테고리_이름_복합검색_성공() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            User owner = createUser(UUID.randomUUID());
            Area area = createArea();
            Category category = createCategory();
            Store store = createStore(owner, area, category);

            Page<Store> pageResult = new PageImpl<>(List.of(store), pageable, 1);

            given(storeRepository.searchStores("한식", "치킨", pageable,null,null)).willReturn(pageResult);

            // when
            PageResponse<StoreResponse> result = storeService.getStores("한식", "치킨", pageable,null,null);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("맛있는 치킨");
        }
    }


    // 가게 단건 조회
    @Nested
    @DisplayName("가게 단건 조회")
    class GetStore {

        @Test
        void 가게_단건조회_성공() {
            // given
            UUID storeId = UUID.randomUUID();
            User owner = createUser(UUID.randomUUID());
            Area area = createArea();
            Category category = createCategory();
            Store store = createStore(owner, area, category);

            given(storeRepository.findByIdWithOwner(storeId)).willReturn(Optional.of(store));

            // when
            StoreResponse result = storeService.getStore(storeId,null,null);

            // then
            assertThat(result.getName()).isEqualTo("맛있는 치킨");
            assertThat(result.getOwnerNickname()).isEqualTo("테스트사장");
        }

        @Test
        void 가게가_존재하지_않으면_예외발생() {
            // given
            UUID storeId = UUID.randomUUID();

            given(storeRepository.findByIdWithOwner(storeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    storeService.getStore(storeId,null,null)
            ).isInstanceOf(BaseException.class);
        }
    }


    // 가게 수정
    @Nested
    @DisplayName("가게 수정")
    class UpdateStore {

        @Test
        void 가게_수정_성공() {
            // given
            UUID userId = UUID.randomUUID();
            User owner = createUser(userId);
            Store store = createStore(owner, createArea(), createCategory());

            given(storeRepository.findByIdWithOwner(any())).willReturn(Optional.of(store));
            given(storeRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("수정된 치킨집")).willReturn(false);

            // Authentication mock
            Authentication authentication = mock(Authentication.class);
            given(authentication.getAuthorities())
                    .willReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_OWNER")));

            StoreUpdateRequest request = new StoreUpdateRequest(
                    "수정된 치킨집", "서울특별시 용산구 이태원로 999", "02-9999-9999"
            );

            // when
            StoreResponse result = storeService.updateStore(UUID.randomUUID(), request, userId, authentication);

            // then
            assertThat(result.getName()).isEqualTo("수정된 치킨집");
        }

        @Test
        void 본인_가게가_아니면_예외발생() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();  // 다른 사용자
            User owner = createUser(ownerId);
            Store store = createStore(owner, createArea(), createCategory());

            given(storeRepository.findByIdWithOwner(any())).willReturn(Optional.of(store));

            // OWNER 권한 → isManagerOrMaster = false → 소유자 체크 진입
            Authentication authentication = mock(Authentication.class);
            given(authentication.getAuthorities())
                    .willReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_OWNER")));

            StoreUpdateRequest request = new StoreUpdateRequest(
                    "수정된 치킨집",
                    "서울특별시 용산구 이태원로 999",
                    null
            );

            // when & then
            assertThatThrownBy(() ->
                    storeService.updateStore(UUID.randomUUID(), request, otherUserId,authentication)
            ).isInstanceOf(BaseException.class);
        }

        @Test
        void 가게가_존재하지_않으면_예외발생() {
            // given
            UUID userId = UUID.randomUUID();

            given(storeRepository.findByIdWithOwner(any())).willReturn(Optional.empty());

            StoreUpdateRequest request = new StoreUpdateRequest(
                    "수정된 치킨집",
                    "서울특별시 용산구 이태원로 999",
                    null
            );

            // when & then
            assertThatThrownBy(() ->
                    storeService.updateStore(UUID.randomUUID(), request, userId,null)
            ).isInstanceOf(BaseException.class);
        }
    }


    // 가게 삭제
    @Nested
    @DisplayName("가게 삭제")
    class DeleteStore {

        @Test
        void 가게_삭제_성공() {
            // given
            UUID userId = UUID.randomUUID();
            User owner = createUser(userId);
            Store store = createStore(owner, createArea(), createCategory());

            given(storeRepository.findByIdWithOwner(any())).willReturn(Optional.of(store));

            Authentication authentication = mock(Authentication.class);
            given(authentication.getAuthorities())
                    .willReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_OWNER")));

            // when
            storeService.deleteStore(UUID.randomUUID(), userId,authentication);

            // then
            assertThat(store.isDeleted()).isTrue();
        }

        @Test
        void 본인_가게가_아니면_예외발생() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            User owner = createUser(ownerId);
            Store store = createStore(owner, createArea(), createCategory());

            given(storeRepository.findByIdWithOwner(any())).willReturn(Optional.of(store));

            // OWNER 권한 → isMaster = false → 소유자 체크 진입
            Authentication authentication = mock(Authentication.class);
            given(authentication.getAuthorities())
                    .willReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_OWNER")));

            // when & then
            assertThatThrownBy(() ->
                    storeService.deleteStore(UUID.randomUUID(), otherUserId,authentication)
            ).isInstanceOf(BaseException.class);
        }

        @Test
        void 가게가_존재하지_않으면_예외발생() {
            // given
            UUID userId = UUID.randomUUID();

            given(storeRepository.findByIdWithOwner(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    storeService.deleteStore(UUID.randomUUID(), userId,null)
            ).isInstanceOf(BaseException.class);
        }
    }


    // 가게 숨김 처리
    @Nested
    @DisplayName("가게 숨김 처리")
    class UpdateHidden {

        @Test
        void 가게_숨김처리_성공() {
            // given
            UUID userId = UUID.randomUUID();
            User owner = createUser(userId);
            Store store = createStore(owner, createArea(), createCategory());

            given(storeRepository.findByIdWithOwner(any())).willReturn(Optional.of(store));

            Authentication authentication = mock(Authentication.class);
            given(authentication.getAuthorities())
                    .willReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_OWNER")));


            StoreHiddenRequest request = new StoreHiddenRequest(true);

            // when
            StoreHiddenResponse result = storeService.updateHidden(UUID.randomUUID(), request, userId,authentication);

            // then
            assertThat(result.getIsHidden()).isTrue();
        }

        @Test
        void 가게_숨김해제_성공() {
            // given
            UUID userId = UUID.randomUUID();
            User owner = createUser(userId);
            Store store = createStore(owner, createArea(), createCategory());
            store.hide(); // 미리 숨김 처리

            given(storeRepository.findByIdWithOwner(any())).willReturn(Optional.of(store));

            Authentication authentication = mock(Authentication.class);
            given(authentication.getAuthorities())
                    .willReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_OWNER")));

            StoreHiddenRequest request = new StoreHiddenRequest(false);

            // when
            StoreHiddenResponse result = storeService.updateHidden(UUID.randomUUID(), request, userId,authentication);

            // then
            assertThat(result.getIsHidden()).isFalse();
        }

        @Test
        void 본인_가게가_아니면_예외발생() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            User owner = createUser(ownerId);
            Store store = createStore(owner, createArea(), createCategory());

            given(storeRepository.findByIdWithOwner(any())).willReturn(Optional.of(store));

            Authentication authentication = mock(Authentication.class);
            given(authentication.getAuthorities())
                    .willReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_OWNER")));


            StoreHiddenRequest request = new StoreHiddenRequest(true);

            // when & then
            assertThatThrownBy(() ->
                    storeService.updateHidden(UUID.randomUUID(), request, otherUserId,authentication)
            ).isInstanceOf(BaseException.class);
        }
    }
}
