package com.sparta.todayeats.order.presentation.controller;

import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.CommonErrorCode;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.order.application.service.OrderServiceV1;
import com.sparta.todayeats.order.domain.entity.OrderStatus;
import com.sparta.todayeats.order.presentation.dto.request.*;
import com.sparta.todayeats.order.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request
            //@AuthenticationPrincipal UserDetailsImpl userDetails  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 아래로 교체
        // CreateOrderResponse data = orderService.createOrder(request, userDetails.getUserId(), userDetails.getRole());
        UUID userId = null; // TODO: JWT 완성 후 제거
        CreateOrderResponse data = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data));
    }

    // ========================================================
// GET /api/v1/orders
// TODO: JWT 완성 후 주석 해제
// - CUSTOMER: 본인 주문만 조회
// - OWNER: 본인 가게 주문만 조회
// - MANAGER: 전체 조회 (soft delete 제외)
// - MASTER: 전체 조회 (삭제된 주문 포함)
// ========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getOrders(
            //@AuthenticationPrincipal UserDetailsImpl userDetails,  // TODO: JWT 완성 후 주석 해제
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String storeName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        // 페이지 사이즈 10/30/50 제한
        validatePageSize(pageable.getPageSize());

        // TODO: JWT 완성 후 아래로 교체
        // Page<OrderSummaryResponse> page = orderService.getOrders(
        //         userDetails.getUserId(), status, storeName, pageable, userDetails.getRole());
        UUID userId = null;  // TODO: JWT 완성 후 제거
        Page<OrderSummaryResponse> page = orderService.getOrders(userId, status, storeName, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.<OrderSummaryResponse>builder()
                        .content(page.getContent())
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .sort(pageable.getSort().toString())
                        .build()
        ));
    }

    // ========================================================
    // GET /api/v1/orders/{orderId}
    // TODO: JWT 완성 후 주석 해제
    // - CUSTOMER: 본인 주문만 조회
    // - OWNER: 본인 가게 주문만 조회
    // - MANAGER/MASTER: 전체 조회
    // ========================================================

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @PathVariable UUID orderId
            //@AuthenticationPrincipal UserDetailsImpl userDetails  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 아래로 교체
        // OrderDetailResponse data = orderService.getOrder(orderId, userDetails.getUserId(), userDetails.getRole());
        OrderDetailResponse data = orderService.getOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ========================================================
    // PUT /api/v1/orders/{orderId}
    // TODO: JWT 완성 후 주석 해제
    // - CUSTOMER 본인만 수정 가능
    // - PENDING 상태만 수정 가능
    // ========================================================

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<UpdateOrderResponse>> updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderRequest request
            //@AuthenticationPrincipal UserDetailsImpl userDetails  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 아래로 교체
        // UpdateOrderResponse data = orderService.updateOrder(orderId, request, userDetails.getUserId(), userDetails.getRole());
        UpdateOrderResponse data = orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ========================================================
    // PATCH /api/v1/orders/{orderId}/status
    // TODO: JWT 완성 후 주석 해제
    // - OWNER: 본인 가게 주문만 변경 가능
    // - MANAGER/MASTER: 전체 변경 가능
    // - CUSTOMER: 상태 변경 불가
    // ========================================================

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<UpdateOrderStatusResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
            //@AuthenticationPrincipal UserDetailsImpl userDetails  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 아래로 교체
        // UpdateOrderStatusResponse data = orderService.updateOrderStatus(orderId, request, userDetails.getUserId(), userDetails.getRole());
        UpdateOrderStatusResponse data = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ========================================================
    // PATCH /api/v1/orders/{orderId}/cancel
    // TODO: JWT 완성 후 주석 해제
    // - CUSTOMER 본인 또는 MASTER만 가능
    // ========================================================

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody(required = false) CancelOrderRequest request
            //@AuthenticationPrincipal UserDetailsImpl userDetails  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 아래로 교체
        // CancelOrderResponse data = orderService.cancelOrder(orderId, request, userDetails.getUserId(), userDetails.getRole());
        CancelOrderResponse data = orderService.cancelOrder(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ========================================================
    // PATCH /api/v1/orders/{orderId}/reject
    // TODO: JWT 완성 후 주석 해제
    // - OWNER: 본인 가게 주문만 거절 가능
    // - MANAGER/MASTER: 전체 거절 가능
    // - CUSTOMER: 거절 불가
    // ========================================================

    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<ApiResponse<RejectOrderResponse>> rejectOrder(
            @PathVariable UUID orderId,
            @RequestBody(required = false) RejectOrderRequest request
            //@AuthenticationPrincipal UserDetailsImpl userDetails  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 아래로 교체
        // RejectOrderResponse data = orderService.rejectOrder(orderId, request, userDetails.getUserId(), userDetails.getRole());
        RejectOrderResponse data = orderService.rejectOrder(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ========================================================
    // DELETE /api/v1/orders/{orderId}
    // TODO: JWT 완성 후 주석 해제
    // - MASTER만 삭제 가능
    // ========================================================

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable UUID orderId
            //@AuthenticationPrincipal UserDetailsImpl userDetails  // TODO: JWT 완성 후 주석 해제
    ) {
        // TODO: JWT 완성 후 아래로 교체
        // orderService.deleteOrder(orderId, userDetails.getUserId(), userDetails.getRole());
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    private void validatePageSize(int pageSize) {
        if (pageSize != 10 && pageSize != 30 && pageSize != 50) {
            throw new BaseException(CommonErrorCode.INVALID_PAGE_SIZE);
        }
    }
}