package com.sparta.todayeats.menu.presentation.controller;

import com.sparta.todayeats.menu.application.service.MenuServiceV1;
import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import com.sparta.todayeats.menu.presentation.dto.request.MenuCreateRequest;
import com.sparta.todayeats.menu.presentation.dto.request.MenuUpdateRequest;
import com.sparta.todayeats.menu.presentation.dto.response.MenuResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MenuControllerV1 {

    private final MenuServiceV1 menuService;

    // 메뉴 등록
    // POST /api/v1/stores/{storeId}/menus
    @PostMapping("/api/v1/stores/{storeId}/menus")
    public MenuResponse createMenu(
            @PathVariable UUID storeId,
            @RequestBody MenuCreateRequest request
    ) {
        MenuEntity menu = menuService.createMenu(storeId, request);
        return MenuResponse.from(menu);
    }

    // 메뉴 목록 조회 - 고객용
    // GET /api/v1/stores/{storeId}/menus
    @GetMapping("/api/v1/stores/{storeId}/menus")
    public Page<MenuResponse> getMenusByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = createPageable(page, size);

        return menuService.getMenusByStore(storeId, keyword, pageable)
                .map(MenuResponse::from);
    }

    // 사장님 메뉴 조회
    // GET /api/v1/stores/{storeId}/menus/owner
    @GetMapping("/api/v1/stores/{storeId}/menus/owner")
    public Page<MenuResponse> getOwnerMenusByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = createPageable(page, size);

        return menuService.getOwnerMenusByStore(storeId, keyword, pageable)
                .map(MenuResponse::from);
    }

    // 메뉴 상세 조회
    // GET /api/v1/stores/{storeId}/menus/{menuId}
    @GetMapping("/api/v1/stores/{storeId}/menus/{menuId}")
    public MenuResponse getMenuDetail(
            @PathVariable UUID storeId,
            @PathVariable UUID menuId
    ) {
        return MenuResponse.from(menuService.getMenuDetail(storeId, menuId));
    }

    // 메뉴 수정
    // PATCH /api/v1/menus/{menuId}
    @PatchMapping("/api/v1/menus/{menuId}")
    public void updateMenu(
            @PathVariable UUID menuId,
            @RequestBody MenuUpdateRequest request
    ) {
        menuService.updateMenu(menuId, request);
    }

    // 메뉴 삭제
    // DELETE /api/v1/menus/{menuId}
    @DeleteMapping("/api/v1/menus/{menuId}")
    public void deleteMenu(@PathVariable UUID menuId) {
        menuService.deleteMenu(menuId);
    }

    // 페이징
    private Pageable createPageable(int page, int size) {
        if (size != 10 && size != 30 && size != 50) {
            throw new IllegalArgumentException("페이지 사이즈는 10, 30, 50만 가능합니다.");
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }
}