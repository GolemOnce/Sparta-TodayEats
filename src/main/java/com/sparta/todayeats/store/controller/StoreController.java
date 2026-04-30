package com.sparta.todayeats.store.controller;

import com.sparta.todayeats.global.annotation.ApiNoContent;
import com.sparta.todayeats.global.annotation.ApiPageable;
import com.sparta.todayeats.global.annotation.LoginUser;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.store.dto.request.StoreCreateRequest;
import com.sparta.todayeats.store.dto.request.StoreHiddenRequest;
import com.sparta.todayeats.store.dto.request.StoreUpdateRequest;
import com.sparta.todayeats.store.dto.response.StoreCreateResponse;
import com.sparta.todayeats.store.dto.response.StoreHiddenResponse;
import com.sparta.todayeats.store.dto.response.StoreResponse;
import com.sparta.todayeats.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Store")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "가게 등록")
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER')")
    public ResponseEntity<ApiResponse<StoreCreateResponse>> createStore(
            @Valid @RequestBody StoreCreateRequest request,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {
        StoreCreateResponse response = storeService.createStore(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    // 가게 목록 조회 + 복합 검색 (카테고리, 이름)
    // CUSTOMER,비로그인: 공개 가게만 노출 / OWNER: 본인거+공개 / MANAGER,MASTER: 전체 노출
    @Operation(summary = "가게 목록 조회")
    @ApiPageable
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StoreResponse>>> getStores(
            @Parameter(description = "카테고리 이름", example = "한식")
            @RequestParam(required = false) String categoryName,
            @Parameter(description = "가게 이름", example = "맛있는 한식당")
            @RequestParam(required = false) String keyword,
            @Parameter(hidden = true)
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {
        PageResponse<StoreResponse> response = storeService.getStores(categoryName, keyword, pageable, authentication, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // CUSTOMER,비로그인: 공개 가게만 노출 / OWNER: 본인거+공게 / MANAGER,MASTER: 전체 노출
    @Operation(summary = "가게 상세 조회")
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStore(
            @Parameter(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            Authentication authentication
    ) {

        StoreResponse response = storeService.getStore(storeId, userId, authentication);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // OWNER는 본인 가게만 수정 가능/ MANAGER, MASTER는 모든 가게 수정 가능
    @Operation(summary = "가게 수정")
    @PatchMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @Parameter(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreUpdateRequest request,
            @Parameter(hidden = true) @LoginUser UUID userId,
            Authentication authentication
    ) {

        StoreResponse response = storeService.updateStore(storeId, request,userId,authentication);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // OWNER는 본인 가게만 삭제 가능/ MASTER는 모든 가게 삭제 가능
    @Operation(summary = "가게 삭제")
    @ApiNoContent
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    public ResponseEntity<Void> deleteStore(
            @Parameter(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            Authentication authentication
    ) {

        storeService.deleteStore(storeId, userId, authentication);

        return ResponseEntity.noContent().build();
    }

    // OWNER는 본인 가게만 가능/ MANAGER, MASTER는 모든 가게 숨김 처리 가능
    @Operation(summary = "가게 숨김 처리")
    @PatchMapping("/{storeId}/hide")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<ApiResponse<StoreHiddenResponse>> updateHidden(
            @Parameter(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreHiddenRequest request,
            @Parameter(hidden = true) @LoginUser UUID userId,
            Authentication authentication
    ) {

        StoreHiddenResponse response = storeService.updateHidden(storeId, request, userId, authentication);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
