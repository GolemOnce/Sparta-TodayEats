package com.sparta.todayeats.menu.controller;

import com.sparta.todayeats.menu.service.MenuService;
import com.sparta.todayeats.menu.entity.Menu;
import com.sparta.todayeats.menu.dto.request.MenuCreateRequest;
import com.sparta.todayeats.menu.dto.request.MenuUpdateRequest;
import com.sparta.todayeats.menu.dto.response.MenuResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.UUID;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // 메뉴 등록
    // POST /api/v1/stores/{storeId}/menus
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PostMapping("/api/v1/stores/{storeId}/menus")
    public MenuResponse createMenu(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody MenuCreateRequest request
    ) {
        Menu menu = menuService.createMenu(storeId, request, userId);
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
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @GetMapping("/api/v1/stores/{storeId}/menus/owner")
    public Page<MenuResponse> getOwnerMenusByStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = createPageable(page, size);

        return menuService.getOwnerMenusByStore(storeId, userId, keyword, pageable)
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
            @Valid @RequestBody MenuUpdateRequest request
    ) {
        menuService.updateMenu(menuId, request);
    }

    // 메뉴 삭제
    // DELETE /api/v1/menus/{menuId}
    @DeleteMapping("/api/v1/menus/{menuId}")
    public void deleteMenu(
            @PathVariable UUID menuId,
            @AuthenticationPrincipal UUID userId
    ) {
        menuService.deleteMenu(menuId, userId);
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