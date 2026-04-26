package com.sparta.todayeats.menu.application.service;

import com.sparta.todayeats.category.domain.entity.Category;
import com.sparta.todayeats.category.domain.repository.CategoryRepository;
import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import com.sparta.todayeats.menu.domain.repository.MenuRepository;
import com.sparta.todayeats.menu.presentation.dto.request.MenuCreateRequest;
import com.sparta.todayeats.menu.presentation.dto.request.MenuStatusUpdateRequest;
import com.sparta.todayeats.menu.presentation.dto.request.MenuUpdateRequest;
import com.sparta.todayeats.store.domain.entity.Store;
import com.sparta.todayeats.store.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuServiceV1 {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    // 메뉴 생성
    @Transactional
    public MenuEntity createMenu(UUID storeId, MenuCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게 없음"));

        MenuEntity menu = MenuEntity.builder()
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .category(category)
                .store(store)
                .isHidden(false)
                .soldOut(false)
                .isDeleted(false)
                .build();

        return menuRepository.save(menu);
    }

    // 사장님 메뉴 조회
    public List<MenuEntity> getOwnerMenusByStore(UUID storeId) {
        return menuRepository.findOwnerMenusByStoreId(storeId);
    }

    // 고객용 가게 메뉴 조회
    public List<MenuEntity> getMenusByStore(UUID storeId) {
        return menuRepository.findOrderableMenusByStoreId(storeId);
    }

    // 고객용 카테고리별 조회
    public List<MenuEntity> getMenusByCategory(UUID storeId, UUID categoryId) {
        return menuRepository.findVisibleMenusByStoreAndCategory(storeId, categoryId);
    }

    // 메뉴 상세 조회
    public MenuEntity getMenuDetail(UUID storeId, UUID menuId) {
        MenuEntity menu = findMenu(menuId);

        if (!menu.getStore().getId().equals(storeId)) {
            throw new IllegalArgumentException("해당 가게의 메뉴가 아닙니다.");
        }

        if (menu.isDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴입니다.");
        }

        return menu;
    }

    // 메뉴 수정
    @Transactional
    public void updateMenu(UUID menuId, MenuUpdateRequest request) {
        MenuEntity menu = findMenu(menuId);
        validateNotDeleted(menu);

        menu.update(
                request.name(),
                request.price(),
                request.description(),
                request.imageUrl()
        );
    }

    // 상태 변경
    @Transactional
    public void updateMenuStatus(UUID menuId, MenuStatusUpdateRequest request) {
        MenuEntity menu = findMenu(menuId);
        validateNotDeleted(menu);

        menu.updateStatus(request.isHidden(), request.soldOut());
    }

    // 메뉴 삭제
    @Transactional
    public void deleteMenu(UUID menuId) {
        MenuEntity menu = findMenu(menuId);
        validateNotDeleted(menu);

        menu.delete();
    }

    private MenuEntity findMenu(UUID menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴 없음"));
    }

    private void validateNotDeleted(MenuEntity menu) {
        if (menu.isDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴입니다.");
        }
    }
}