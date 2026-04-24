package com.sparta.todayeats.store.controller;

import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.store.dto.StoreCreateResponse;
import com.sparta.todayeats.store.service.StoreService;
import com.sparta.todayeats.store.dto.StoreCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // TODO: 권한 처리(OWNER), Auditing
    // TODO: 인증 구현 후 토큰에서 꺼내기 -> userId
    // 가게 생성
    @PostMapping
    public ResponseEntity<ApiResponse<StoreCreateResponse>> createStore(@Valid @RequestBody StoreCreateRequest request, @RequestHeader("X-User-Id") UUID userId) {
        StoreCreateResponse response = storeService.createStore(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }
}
