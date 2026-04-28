package com.sparta.todayeats.menu.service;

import com.sparta.todayeats.ai.service.AiService;
import com.sparta.todayeats.category.domain.entity.Category;
import com.sparta.todayeats.category.domain.repository.CategoryRepository;
import com.sparta.todayeats.menu.entity.Menu;
import com.sparta.todayeats.menu.repository.MenuRepository;
import com.sparta.todayeats.menu.dto.request.MenuCreateRequest;
import com.sparta.todayeats.menu.dto.request.MenuStatusUpdateRequest;
import com.sparta.todayeats.menu.dto.request.MenuUpdateRequest;
import com.sparta.todayeats.store.entity.Store;
import com.sparta.todayeats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final AiService aiService;

    // 메뉴 생성
    @Transactional
    public Menu createMenu(UUID storeId, MenuCreateRequest request, UUID userId) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게 없음"));

        validateStoreOwner(store, userId);

        String description = request.description();

        if (request.aiDescription()) {
            description = aiService
                    .generateProductDescription(
                            request.name() + " 상품 설명을 추천해줘",
                            userId
                    )
                    .description();
        }

        Menu menu = Menu.builder()
                .name(request.name())
                .price(request.price())
                .description(description)
                .imageUrl(request.imageUrl())
                .category(category)
                .store(store)
                .isHidden(false)
                .soldOut(false)
                .build();

        return menuRepository.save(menu);
    }

    // 사장님 메뉴 조회
    public Page<Menu> getOwnerMenusByStore(
            UUID storeId,
            UUID userId,
            String keyword,
            Pageable pageable
    ) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게 없음"));

        validateStoreOwner(store, userId);

        return menuRepository.findOwnerMenusByStoreId(storeId, keyword, pageable);
    }

    // 고객용 가게 메뉴 조회
    public Page<Menu> getMenusByStore(
            UUID storeId,
            String keyword,
            Pageable pageable
    ) {
        return menuRepository.findOrderableMenusByStoreId(storeId, keyword, pageable);
    }

    // 고객용 카테고리별 조회
    public Page<Menu> getMenusByCategory(
            UUID storeId,
            UUID categoryId,
            String keyword,
            Pageable pageable
    ) {
        return menuRepository.findVisibleMenusByStoreAndCategory(
                storeId,
                categoryId,
                keyword,
                pageable
        );
    }

    // 메뉴 상세 조회
    public Menu getMenuDetail(UUID storeId, UUID menuId) {
        Menu menu = findMenu(menuId);

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
    public void updateMenu(UUID menuId, MenuUpdateRequest request, UUID userId) {
        Menu menu = findMenu(menuId);
        validateNotDeleted(menu);
        validateStoreOwner(menu.getStore(), userId);

        menu.update(
                request.name(),
                request.price(),
                request.description(),
                request.imageUrl()
        );
    }

    // 상태 변경
    @Transactional
    public void updateMenuStatus(UUID menuId, MenuStatusUpdateRequest request, UUID userId) {
        Menu menu = findMenu(menuId);
        validateNotDeleted(menu);
        validateStoreOwner(menu.getStore(), userId);

        menu.updateStatus(request.isHidden(), request.soldOut());
    }

    // 메뉴 삭제
    @Transactional
    public void deleteMenu(UUID menuId, UUID userId) {
        Menu menu = findMenu(menuId);
        validateNotDeleted(menu);
        validateStoreOwner(menu.getStore(), userId);

        menu.delete(userId);
    }

    // AI 설명
//    @Transactional
//    public MenuResponse createMenu(UUID storeId, MenuCreateRequest request, UUID userId) {
//        Category category = categoryRepository.findById(request.categoryId())
//                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));
//
//        Store store = storeRepository.findById(storeId)
//                .orElseThrow(() -> new IllegalArgumentException("가게 없음"));
//
//        String description = request.description();
//
//        if (request.aiDescription()) {
//            description = aiService
//                    .generateProductDescription(request.name() + " 상품 설명을 추천해줘", userId)
//                    .description();
//        }
//
//        Menu menu = Menu.builder()
//                .name(request.name())
//                .price(request.price())
//                .description(description)
//                .imageUrl(request.imageUrl())
//                .category(category)
//                .store(store)
//                .isHidden(false)
//                .soldOut(false)
//                .build();
//
//        Menu savedMenu = menuRepository.save(menu);
//
//        return MenuResponse.from(savedMenu);
//    }

    private Menu findMenu(UUID menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴 없음"));
    }

    private void validateNotDeleted(Menu menu) {
        if (menu.isDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴입니다.");
        }
    }

    private void validateStoreOwner(Store store, UUID userId) {
        if (store == null) {
            throw new IllegalArgumentException("메뉴에 연결된 가게가 없습니다.");
        }

        if (store.getOwner() == null) {
            throw new IllegalArgumentException("가게에 연결된 사장님이 없습니다.");
        }

        if (!store.getOwner().getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 가게의 사장님만 접근할 수 있습니다.");
        }
    }
}