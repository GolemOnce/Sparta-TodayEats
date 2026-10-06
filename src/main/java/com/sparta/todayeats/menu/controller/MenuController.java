package com.sparta.todayeats.menu.controller;

import com.sparta.todayeats.global.annotation.ApiNoContent;
import com.sparta.todayeats.global.annotation.ApiPageable;
import com.sparta.todayeats.global.annotation.LoginUser;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.menu.service.MenuService;
import com.sparta.todayeats.menu.entity.Menu;
import com.sparta.todayeats.menu.dto.request.MenuCreateRequest;
import com.sparta.todayeats.menu.dto.request.MenuStatusUpdateRequest;
import com.sparta.todayeats.menu.dto.request.MenuUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Tag(name = "Menu")
@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // POST /api/v1/stores/{storeId}/menus
    @Operation(summary = "메뉴 등록")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PostMapping("/api/v1/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<MenuCreateResponse>> createMenu(
            @Parameter(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Valid @RequestBody MenuCreateRequest request
    ) {
        Menu menu = menuService.createMenu(storeId, request, userId);

        MenuCreateResponse response = MenuCreateResponse.from(menu);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    // GET /api/v1/stores/{storeId}/menus
    @Operation(summary = "메뉴 목록 조회 - 고객용")
    @ApiPageable
    @GetMapping("/api/v1/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<PageResponse<MenuDetailResponse>>> getMenusByStore(
            @Parameter(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId,
            @Parameter(description = "메뉴 이름", example = "고기듬뿍 고향만두")
            @RequestParam(required = false) String keyword,
            @Parameter(hidden = true) @RequestParam(defaultValue = "0") int page,
            @Parameter(hidden = true) @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = createPageable(page, size);

        Page<Menu> menus = menuService.getMenusByStore(storeId, keyword, pageable);

        PageResponse<MenuDetailResponse> response = PageResponse.from(
                menus, menus.map(MenuDetailResponse::from).getContent()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET /api/v1/stores/{storeId}/menus/owner
    @Operation(summary = "메뉴 목록 조회 - 가게 주인용")
    @ApiPageable
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @GetMapping("/api/v1/stores/{storeId}/menus/owner")
    public ResponseEntity<ApiResponse<PageResponse<MenuDetailResponse>>> getOwnerMenusByStore(
            @Parameter(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Parameter(description = "메뉴 이름", example = "고기듬뿍 고향만두")
            @RequestParam(required = false) String keyword,
            @Parameter(hidden = true) @RequestParam(defaultValue = "0") int page,
            @Parameter(hidden = true) @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = createPageable(page, size);

        Page<Menu> menus = menuService.getOwnerMenusByStore(storeId, userId, keyword, pageable);

        PageResponse<MenuDetailResponse> response = PageResponse.from(
                menus, menus.map(MenuDetailResponse::from).getContent()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET /api/v1/stores/{storeId}/menus/{menuId}
    @Operation(summary = "메뉴 상세 조회")
    @GetMapping("/api/v1/stores/{storeId}/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> getMenuDetail(
            @Parameter(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId,
            @Parameter(description = "메뉴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID menuId
    ) {
        Menu menu = menuService.getMenuDetail(storeId, menuId);

        MenuDetailResponse response = MenuDetailResponse.from(menu);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // PATCH /api/v1/menus/{menuId}
    @Operation(summary = "메뉴 수정")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> updateMenu(
            @Parameter(description = "메뉴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID menuId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Valid @RequestBody MenuUpdateRequest request

    ) {
        Menu menu = menuService.updateMenu(menuId, userId, request);

        MenuDetailResponse response = MenuDetailResponse.from(menu);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // PATCH /api/v1/menus/{menuId}/status
    @Operation(summary = "메뉴 상태 변경")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @PatchMapping("/api/v1/menus/{menuId}/status")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> updateMenuStatus(
            @Parameter(description = "메뉴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID menuId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Valid @RequestBody MenuStatusUpdateRequest request
    ) {
        Menu menu = menuService.updateMenuStatus(menuId, userId, request);

        MenuDetailResponse response = MenuDetailResponse.from(menu);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메뉴 삭제
    // DELETE /api/v1/menus/{menuId}
    @Operation(summary = "메뉴 삭제")
    @ApiNoContent
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @DeleteMapping("/api/v1/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @Parameter(description = "메뉴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID menuId,
            @Parameter(hidden = true) @LoginUser UUID userId
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