package com.sparta.todayeats.store.controller;

import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.store.dto.request.StoreCreateRequest;
import com.sparta.todayeats.store.dto.request.StoreHiddenRequest;
import com.sparta.todayeats.store.dto.request.StoreUpdateRequest;
import com.sparta.todayeats.store.dto.response.StoreCreateResponse;
import com.sparta.todayeats.store.dto.response.StoreHiddenResponse;
import com.sparta.todayeats.store.dto.response.StoreResponse;
import com.sparta.todayeats.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 가게 생성
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER')")
    public ResponseEntity<ApiResponse<StoreCreateResponse>> createStore(@Valid @RequestBody StoreCreateRequest request,@AuthenticationPrincipal UUID userId) {
        StoreCreateResponse response = storeService.createStore(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    // TODO: CUSTOMER → isHidden = false 인 것만 노출
    // TODO: OWNER/MANAGER/MASTER → 숨김 포함 전체 노출
    // 가게 목록 조회 + 복합 검색 (카테고리, 이름)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StoreResponse>>> getStores(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<StoreResponse> response = storeService.getStores(categoryName, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    // 가게 단건 조회
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStore(
            @PathVariable UUID storeId) {

        StoreResponse response = storeService.getStore(storeId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 가게 수정
    @PatchMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreUpdateRequest request,@AuthenticationPrincipal UUID userId) {

        StoreResponse response = storeService.updateStore(storeId, request,userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 가게 삭제
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
            @PathVariable UUID storeId,@AuthenticationPrincipal UUID userId) {

        storeService.deleteStore(storeId, userId);

        return ResponseEntity.noContent().build();
    }

    // 가게 숨김 처리
    @PatchMapping("/{storeId}/hide")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<ApiResponse<StoreHiddenResponse>> updateHidden(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreHiddenRequest request,@AuthenticationPrincipal UUID userId) {

        StoreHiddenResponse response = storeService.updateHidden(storeId, request, userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
