package com.sparta.todayeats.menu.controller;

import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.menu.service.MenuService;
import com.sparta.todayeats.menu.entity.Menu;
import com.sparta.todayeats.menu.dto.request.MenuCreateRequest;
import com.sparta.todayeats.menu.dto.request.MenuStatusUpdateRequest;
import com.sparta.todayeats.menu.dto.request.MenuUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.menu.dto.response.MenuCreateResponse;
import com.sparta.todayeats.menu.dto.response.MenuDetailResponse;

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
    public ResponseEntity<ApiResponse<MenuCreateResponse>> createMenu(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody MenuCreateRequest request
    ) {
        Menu menu = menuService.createMenu(storeId, request, userId);

        MenuCreateResponse response = MenuCreateResponse.from(menu);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    // 메뉴 목록 조회 - 고객용
    // GET /api/v1/stores/{storeId}/menus
    @GetMapping("/api/v1/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<PageResponse<MenuDetailResponse>>> getMenusByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = createPageable(page, size);

        Page<Menu> menus = menuService.getMenusByStore(storeId, keyword, pageable);

        PageResponse<MenuDetailResponse> response = PageResponse.from(
                menus, menus.map(MenuDetailResponse::from).getContent()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 사장님 메뉴 조회
    // GET /api/v1/stores/{storeId}/menus/owner
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @GetMapping("/api/v1/stores/{storeId}/menus/owner")
    public ResponseEntity<ApiResponse<PageResponse<MenuDetailResponse>>> getOwnerMenusByStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = createPageable(page, size);

        Page<Menu> menus = menuService.getOwnerMenusByStore(storeId, userId, keyword, pageable);

        PageResponse<MenuDetailResponse> response = PageResponse.from(
                menus, menus.map(MenuDetailResponse::from).getContent()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메뉴 상세 조회
    // GET /api/v1/stores/{storeId}/menus/{menuId}
    @GetMapping("/api/v1/stores/{storeId}/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> getMenuDetail(
            @PathVariable UUID storeId,
            @PathVariable UUID menuId
    ) {
        Menu menu = menuService.getMenuDetail(storeId, menuId);

        MenuDetailResponse response = MenuDetailResponse.from(menu);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메뉴 수정
    // PATCH /api/v1/menus/{menuId}
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> updateMenu(
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody MenuUpdateRequest request

    ) {
        Menu menu = menuService.updateMenu(menuId, userId, request);

        MenuDetailResponse response = MenuDetailResponse.from(menu);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메뉴 상태 변경
    // PATCH /api/v1/menus/{menuId}/status
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/api/v1/menus/{menuId}/status")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> updateMenuStatus(
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody MenuStatusUpdateRequest request
    ) {
        Menu menu = menuService.updateMenuStatus(menuId, userId, request);

        MenuDetailResponse response = MenuDetailResponse.from(menu);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메뉴 삭제
    // DELETE /api/v1/menus/{menuId}
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @DeleteMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @AuthenticationPrincipal UUID userId
    ) {
        menuService.deleteMenu(menuId, userId);

        return ResponseEntity.noContent().build();
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