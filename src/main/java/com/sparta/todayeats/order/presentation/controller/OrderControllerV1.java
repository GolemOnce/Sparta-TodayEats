package com.sparta.todayeats.order.presentation.controller;

import com.sparta.todayeats.order.application.service.OrderServiceV1;
import com.sparta.todayeats.order.presentation.dto.request.CreateOrderRequest;
import com.sparta.todayeats.order.presentation.dto.response.CreateOrderResponse;
import com.sparta.todayeats.order.presentation.dto.response.OrderSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderControllerV1 {

    private final OrderServiceV1 orderService;

    // ========================================================
    // POST /api/v1/orders
    // TODO: CUSTOMER 권한 체크 추가
    // ========================================================

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UUID userId) {

        CreateOrderResponse data = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(HttpStatus.CREATED.value(), "CREATED", data));
    }

    // ========================================================
    // GET /api/v1/orders
    // TODO: CUSTOMER 권한 체크 추가
    // ========================================================

    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<OrderSummaryResponse> page = orderService.getOrders(userId, pageable);
        return ResponseEntity.ok(successPage(page, pageable));
    }

    // ─── 공통 응답 빌더 ───────────────────────────────────────────────────

    private Map<String, Object> success(int status, String message, Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("data", data);
        return body;
    }

    private Map<String, Object> successPage(Page<?> page, Pageable pageable) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", page.getContent());     // 컨벤션: content 키 사용
        data.put("page", page.getNumber());
        data.put("size", page.getSize());
        data.put("totalElements", page.getTotalElements());
        data.put("totalPages", page.getTotalPages());
        data.put("sort", pageable.getSort().toString());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "SUCCESS");
        body.put("data", data);
        return body;
    }
}