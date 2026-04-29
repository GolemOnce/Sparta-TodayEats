package com.sparta.todayeats.store.service;

import com.sparta.todayeats.area.entity.Area;
import com.sparta.todayeats.area.repository.AreaRepository;
import com.sparta.todayeats.category.entity.Category;
import com.sparta.todayeats.category.repository.CategoryRepository;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.global.exception.*;
import com.sparta.todayeats.menu.service.MenuService;
import com.sparta.todayeats.store.dto.request.StoreCreateRequest;
import com.sparta.todayeats.store.dto.request.StoreHiddenRequest;
import com.sparta.todayeats.store.dto.request.StoreUpdateRequest;
import com.sparta.todayeats.store.dto.response.StoreCreateResponse;
import com.sparta.todayeats.store.dto.response.StoreHiddenResponse;
import com.sparta.todayeats.store.dto.response.StoreResponse;
import com.sparta.todayeats.store.entity.Store;
import com.sparta.todayeats.store.repository.StoreRepository;
import com.sparta.todayeats.user.entity.User;
import com.sparta.todayeats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final MenuService menuService;
    private final StoreRepository storeRepository;
    private final AreaRepository areaRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // 가게 생성
    @Transactional
    public StoreCreateResponse createStore(StoreCreateRequest request, UUID userId) {
        // 운영 지역 조회, 활성화 여부 확인
        Area area = getAreaEntity(request.getAreaId());
        validateAreaActive(area);

        // 가게 이름 정규화, 중복 검증
        String name = normalizeStoreName(request.getName());
        validateDuplicateStore(name);

        // 소유자, 카테고리 조회
        User owner = getUserEntity(userId);
        Category category = getCategoryEntity(request.getCategoryId());

        // 가게 엔티티 생성
        Store store = Store.builder()
                .owner(owner)
                .area(area)
                .category(category)
                .name(name)
                .address(request.getAddress())
                .phone(normalizePhone(request.getPhone()))
                .build();

        // DB 저장 (동시성 문제 해결)
        Store saved;
        try {
            saved = storeRepository.save(store);
        } catch (DataIntegrityViolationException e) {
            throw new BaseException(StoreErrorCode.STORE_ALREADY_EXISTS);
        }

        return toCreateResponse(saved);
    }


    // 가게 목록 조회 && 검색 (카테고리 + 이름 복합 필터)
    public PageResponse<StoreResponse> getStores(String categoryName, String keyword, Pageable pageable, Authentication authentication, UUID userId) {
        // QueryDSL 동적 쿼리로 조회 (+ 권한)
        Page<Store> result = storeRepository.searchStores(categoryName, keyword, pageable,authentication,userId);

        // 엔티티 → DTO 변환
        List<StoreResponse> content = result.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        // 페이지 응답 DTO 생성
        return PageResponse.<StoreResponse>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .sort(pageable.getSort().toString())
                .build();
    }


    // 가게 단건 조회
    public StoreResponse getStore(UUID storeId, UUID userId, Authentication authentication) {
        // 가게 엔티티 조회
        Store store = getStoreEntity(storeId);

        // 숨김 아니면 누구나 조회 가능
        if (!store.getIsHidden()) {
            return toResponse(store);
        }

        // 숨김이면 권한 체크
        if (!canAccessHiddenStore(store, userId, authentication)) {
            throw new BaseException(StoreErrorCode.STORE_NOT_VISIBLE);
        }

        return toResponse(store);
    }


    // 가게 수정
    @Transactional
    public StoreResponse updateStore(UUID storeId, StoreUpdateRequest request, UUID userId,Authentication authentication) {
        // 수정 대상 가게 조회
        Store store = getStoreEntity(storeId);

        // MANAGER, MASTER는 모든 가게 수정 가능 / OWNER는 본인 가게만 수정 가능
        boolean isManagerOrMaster = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_MASTER"));

        if (!isManagerOrMaster) {
            validateStoreOwner(store, userId);
        }

        // 정규화, 이름 중복 체크
        String name = normalizeStoreName(request.getName());
        if (!store.getName().equalsIgnoreCase(name)) {
            validateDuplicateStore(name);
        }

        // 전체 교체
        store.update(name, request.getAddress(), normalizePhone(request.getPhone()));

        return toResponse(store);
    }


    // 가게 삭제
    @Transactional
    public void deleteStore(UUID storeId, UUID userId, Authentication authentication) {
        // 삭제 대상 가게 조회
        Store store = getStoreEntity(storeId);

        // MASTER는 모든 가게 삭제 가능 / OWNER는 본인 가게만 삭제 가능
        boolean isMaster = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MASTER"));

        if (!isMaster) {
            validateStoreOwner(store, userId);
        }

        // 소프트 삭제
        store.softDelete(userId);
    }


    // 가게 숨김 처리
    @Transactional
    public StoreHiddenResponse updateHidden(UUID storeId, StoreHiddenRequest request, UUID userId,  Authentication authentication) {
        // 숨김 처리 대상 가게 조회
        Store store = getStoreEntity(storeId);

        // MANAGER, MASTER는 모든 가게 숨김 처리 가능 / OWNER는 본인 가게만 가능
        boolean isManagerOrMaster = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_MASTER"));

        if (!isManagerOrMaster) {
            validateStoreOwner(store, userId);
        }

        // 숨김 처리
        if (request.getIsHidden()) {
            store.hide();
        } else {
            store.show();
        }

        return toHiddenResponse(store);
    }



    // 소유자 엔티티 조회
    private User getUserEntity(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
    }

    // 가게 엔티티 조회
    private Store getStoreEntity(UUID storeId) {
        return storeRepository.findByIdWithOwner(storeId)
                .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
    }

    // 운영 지역 엔티티 조회
    private Area getAreaEntity(UUID areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() -> new BaseException(AreaErrorCode.AREA_NOT_FOUND));
    }

    // 카테고리 엔티티 조회
    private Category getCategoryEntity(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BaseException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }

    // 운영 지역 활성화 여부 확인
    private void validateAreaActive(Area area) {
        if (!area.getIsActive()) {
            throw new BaseException(AreaErrorCode.AREA_INACTIVE);
        }
    }

    // 가게  이름 중복 여부 확인 (삭제 안 된 것만 중복 체크)
    private void validateDuplicateStore(String name) {
        if (storeRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(name)) {
            throw new BaseException(StoreErrorCode.STORE_ALREADY_EXISTS);
        }
    }

    // 본인 가게인지 검증
    private void validateStoreOwner(Store store, UUID userId) {
        if (!store.getOwner().getUserId().equals(userId)) {
            throw new BaseException(StoreErrorCode.STORE_FORBIDDEN);
        }
    }

    // 가게 이름 정규화
    private String normalizeStoreName(String name) {
        if (name == null) {
            throw new BaseException(StoreErrorCode.INVALID_STORE_NAME);
        }
        String normalized = name.trim();

        if (normalized.isBlank()) {
            throw new BaseException(StoreErrorCode.INVALID_STORE_NAME);
        }

        return normalized;
    }

    // 핸드폰 번호 정규화
    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }

    // 권한 체크
    private boolean canAccessHiddenStore(Store store, UUID userId, Authentication authentication) {

        boolean isManagerOrMaster = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")
                                || a.getAuthority().equals("ROLE_MASTER"));

        boolean isOwner = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        boolean isMyStore = userId != null &&
                store.getOwner().getUserId().equals(userId);

        return isManagerOrMaster || (isOwner && isMyStore);
    }

    // Store 엔티티 → 생성 응답 DTO 변환
    private StoreCreateResponse toCreateResponse(Store store) {
        return StoreCreateResponse.builder()
                .storeId(store.getId())
                .ownerNickname(store.getOwner().getNickname())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .areaId(store.getArea().getId())
                .areaName(store.getArea().getName())
                .categoryId(store.getCategory().getId())
                .categoryName(store.getCategory().getName())
                .createdAt(store.getCreatedAt())
                .createdBy(store.getCreatedBy())
                .build();
    }

    // Store 엔티티 → 응답 DTO 변환
    private StoreResponse toResponse(Store store) {
        return StoreResponse.builder()
                .storeId(store.getId())
                .ownerNickname(store.getOwner().getNickname())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .areaId(store.getArea().getId())
                .areaName(store.getArea().getName())
                .categoryId(store.getCategory().getId())
                .categoryName(store.getCategory().getName())
                .averageRating(store.getAverageRating())
                .isHidden(store.getIsHidden())
                .createdAt(store.getCreatedAt())
                .createdBy(store.getCreatedBy())
                .updatedAt(store.getUpdatedAt())
                .updatedBy(store.getUpdatedBy())
                .build();
    }

    // Store 엔티티 → 숨김 응답 DTO 변환
    private StoreHiddenResponse toHiddenResponse(Store store) {
        return StoreHiddenResponse.builder()
                .storeId(store.getId())
                .isHidden(store.getIsHidden())
                .createdAt(store.getCreatedAt())
                .createdBy(store.getCreatedBy())
                .updatedAt(store.getUpdatedAt())
                .updatedBy(store.getUpdatedBy())
                .build();
    }

    // 가게 연쇄 삭제
    @Transactional
    public void deleteAllStoresByUserId(UUID targetUserId, UUID currentUserId) {
        // 가게 목록
        List<Store> ownerStores = storeRepository.findAllByOwnerUserId(targetUserId);

        for (Store store : ownerStores) {
            // 가게 메뉴 Soft Delete
            menuService.deleteAllMenusByStoreId(store.getId(), currentUserId);

            // 가게 Soft Delete
            store.softDelete(currentUserId);
        }
    }
}
