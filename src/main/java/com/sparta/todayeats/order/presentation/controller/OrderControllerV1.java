package com.sparta.todayeats.order.presentation.controller;

import com.sparta.todayeats.order.application.service.OrderServiceV1;
import com.sparta.todayeats.order.presentation.dto.request.CreateOrderRequest;
import com.sparta.todayeats.order.presentation.dto.response.CreateOrderResponse;
import com.sparta.todayeats.order.presentation.dto.response.OrderDetailResponse;
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
    // TODO: JWT 완성 후 주석 해제
    // - CUSTOMER만 주문 생성 가능
    // ========================================================

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UUID userId
            //@AuthenticationPrincipal UserDetailsImpl userDetails  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 아래로 교체
        // CreateOrderResponse data = orderService.createOrder(request, userDetails.getUserId(), userDetails.getRole());
        CreateOrderResponse data = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(HttpStatus.CREATED.value(), "CREATED", data));
    }

    // ========================================================
    // GET /api/v1/orders
    // TODO: JWT 완성 후 주석 해제
    // - CUSTOMER: 본인 주문만 조회
    // - OWNER: 본인 가게 주문만 조회
    // - MANAGER/MASTER: 전체 조회
    // ========================================================

    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders(
            @AuthenticationPrincipal UUID userId,
            //@AuthenticationPrincipal UserDetailsImpl userDetails,  // TODO: JWT 완성 후 주석 해제
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        // TODO: JWT 완성 후 아래로 교체
        // Page<OrderSummaryResponse> page = orderService.getOrders(userDetails.getUserId(), pageable, userDetails.getRole());
        Page<OrderSummaryResponse> page = orderService.getOrders(userId, pageable);
        return ResponseEntity.ok(successPage(page, pageable));
    }

    // ========================================================
    // GET /api/v1/orders/{orderId}
    // TODO: JWT 완성 후 주석 해제
    // - CUSTOMER: 본인 주문만 조회
    // - OWNER: 본인 가게 주문만 조회
    // - MANAGER/MASTER: 전체 조회
    // ========================================================

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(
            @PathVariable UUID orderId
            //@AuthenticationPrincipal UserDetailsImpl userDetails  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 아래로 교체
        // OrderDetailResponse data = orderService.getOrder(orderId, userDetails.getUserId(), userDetails.getRole());
        OrderDetailResponse data = orderService.getOrder(orderId);
        return ResponseEntity.ok(success(HttpStatus.OK.value(), "SUCCESS", data));
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