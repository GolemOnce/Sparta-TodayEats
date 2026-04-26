package com.sparta.todayeats.menu.presentation.controller;

import com.sparta.todayeats.menu.application.service.MenuServiceV1;
import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import com.sparta.todayeats.menu.presentation.dto.request.MenuCreateRequest;
import com.sparta.todayeats.menu.presentation.dto.request.MenuUpdateRequest;
import com.sparta.todayeats.menu.presentation.dto.response.MenuResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public List<MenuResponse> getMenusByStore(@PathVariable UUID storeId) {
        return menuService.getMenusByStore(storeId).stream()
                .map(MenuResponse::from)
                .toList();
    }

    // 사장님 메뉴 조회
    // GET /api/v1/stores/{storeId}/menus/owner
    @GetMapping("/api/v1/stores/{storeId}/menus/owner")
    public List<MenuResponse> getOwnerMenusByStore(@PathVariable UUID storeId) {
        return menuService.getOwnerMenusByStore(storeId).stream()
                .map(MenuResponse::from)
                .toList();
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
}