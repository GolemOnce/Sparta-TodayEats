package com.sparta.todayeats.address.controller;

import com.sparta.todayeats.address.dto.request.AddressCreateRequest;
import com.sparta.todayeats.address.dto.request.AddressUpdateRequest;
import com.sparta.todayeats.address.dto.response.*;
import com.sparta.todayeats.address.service.AddressService;
import com.sparta.todayeats.global.annotation.ApiNoContent;
import com.sparta.todayeats.global.annotation.ApiPageable;
import com.sparta.todayeats.global.annotation.LoginUser;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Address")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "배송지 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<AddressCreateResponse>> createAddress(
            @Parameter(hidden = true) @LoginUser UUID userId,
            @RequestBody AddressCreateRequest request
    ){
        AddressCreateResponse response = addressService.createAddress(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "내 배송지 목록 조회")
    @ApiPageable
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AddressResponse>>> getPagedAddress(
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Parameter(hidden = true) Pageable pageable
    ) {
        PageResponse<AddressResponse> response = addressService.getPagedAddresses(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "배송지 상세 조회")
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getDetailAddress(
            @Parameter(description = "배송지 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID addressId,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {
        AddressResponse response = addressService.getDetailAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "배송지 수정")
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressUpdateResponse>> updateAddress(
            @Parameter(description = "배송지 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID addressId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            @RequestBody AddressUpdateRequest request
    ) {
        AddressUpdateResponse response = addressService.updateAddress(userId, addressId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "기본 배송지 설정")
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressDefaultResponse>> setDefaultAddress(
            @Parameter(description = "배송지 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID addressId,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {
        AddressDefaultResponse response = addressService.setDefaultAddress(userId, addressId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "배송지 삭제")
    @ApiNoContent
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "배송지 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID addressId,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {
        addressService.deleteAddress(userId, addressId);

        return ResponseEntity.noContent().build();
    }
}
