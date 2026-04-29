package com.sparta.todayeats.address.controller;

import com.sparta.todayeats.address.dto.request.AddressCreateRequest;
import com.sparta.todayeats.address.dto.request.AddressUpdateRequest;
import com.sparta.todayeats.address.dto.response.*;
import com.sparta.todayeats.address.service.AddressService;
import com.sparta.todayeats.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    // 배송지 등록
    @PostMapping
    public ResponseEntity<ApiResponse<AddressCreateResponse>> createAddress(
            @AuthenticationPrincipal UUID userId,
            @RequestBody AddressCreateRequest request
    ){
        AddressCreateResponse response = addressService.createAddress(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // 배송지 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<AddressPageResponse>> getPagedAddress(
            @AuthenticationPrincipal UUID userId,
            Pageable pageable
    ) {
        AddressPageResponse response = addressService.getPagedAddresses(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 배송지 상세 조회
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressDetailResponse>> getDetailAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal UUID userId
    ) {
        AddressDetailResponse response = addressService.getDetailAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 배송지 수정
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressUpdateResponse>> updateAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody AddressUpdateRequest request
    ) {
        AddressUpdateResponse response = addressService.updateAddress(userId, addressId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
    // 기본 배송지 설정
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressDefaultResponse>> setDefaultAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal UUID userId
    ) {
        AddressDefaultResponse response = addressService.setDefaultAddress(userId, addressId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 배송지 삭제
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal UUID userId) {
        addressService.deleteAddress(userId, addressId);

        return ResponseEntity.noContent().build();
    }
}
