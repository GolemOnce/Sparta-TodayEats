package com.sparta.todayeats.order.controller;

import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.CommonErrorCode;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.order.dto.request.*;
import com.sparta.todayeats.order.dto.response.*;
import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.order.service.OrderService;
import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 주문 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     * POST /api/v1/orders
     * CUSTOMER만 주문 생성 가능 (JWT 완성 후 활성화)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UUID userId,
            Authentication authentication
    ) {
        UserRoleEnum role = extractRole(authentication);
        CreateOrderResponse data = orderService.createOrder(request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data));
    }

    /**
     * 주문 목록 조회
     * GET /api/v1/orders
     * CUSTOMER: 본인 주문만 조회, OWNER: 본인 가게 주문만 조회
     * MANAGER: 전체 조회(soft delete 제외), MASTER: 전체 조회(삭제 포함)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getOrders(
            @AuthenticationPrincipal UUID userId,
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String storeName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        // 페이지 사이즈 10/30/50 제한
        validatePageSize(pageable.getPageSize());
        UserRoleEnum role = extractRole(authentication);
        Page<OrderSummaryResponse> page = orderService.getOrders(userId, status, storeName, pageable, role);

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

    /**
     * 주문 단건 조회
     * GET /api/v1/orders/{orderId}
     * CUSTOMER: 본인 주문만, OWNER: 본인 가게 주문만, MANAGER/MASTER: 전체 - JWT 완성 후 활성화
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId,
            Authentication authentication
    ) {
        UserRoleEnum role = extractRole(authentication);
        OrderDetailResponse data = orderService.getOrder(orderId, userId, role);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 주문 요청사항 수정
     * PUT /api/v1/orders/{orderId}
     * PENDING 상태에서 CUSTOMER 본인만 수정 가능
     */
    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<UpdateOrderResponse>> updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderRequest request,
            @AuthenticationPrincipal UUID userId,
            Authentication authentication
    ) {
        UserRoleEnum role = extractRole(authentication);
        UpdateOrderResponse data = orderService.updateOrder(orderId, request, userId, role);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 주문 상태 변경
     * PATCH /api/v1/orders/{orderId}/status
     * OWNER: 본인 가게 주문만, MANAGER/MASTER: 전체, CUSTOMER: 불가
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<UpdateOrderStatusResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UUID userId,
            Authentication authentication
    ) {
        UserRoleEnum role = extractRole(authentication);
        UpdateOrderStatusResponse data = orderService.updateOrderStatus(orderId, request, userId, role);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 주문 취소
     * PATCH /api/v1/orders/{orderId}/cancel
     * PENDING 상태에서 5분 이내, CUSTOMER 본인 또는 MASTER만 가능
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody(required = false) @Valid CancelOrderRequest request,
            @AuthenticationPrincipal UUID userId,
            Authentication authentication
    ) {
        UserRoleEnum role = extractRole(authentication);
        CancelOrderResponse data = orderService.cancelOrder(orderId, request, userId, role);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 주문 거절
     * PATCH /api/v1/orders/{orderId}/reject
     * PENDING 상태에서 OWNER: 본인 가게만, MANAGER/MASTER: 전체, CUSTOMER: 불가
     */
    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<ApiResponse<RejectOrderResponse>> rejectOrder(
            @PathVariable UUID orderId,
            @RequestBody(required = false) @Valid RejectOrderRequest request,
            @AuthenticationPrincipal UUID userId,
            Authentication authentication
    ) {
        UserRoleEnum role = extractRole(authentication);
        RejectOrderResponse data = orderService.rejectOrder(orderId, request, userId, role);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 주문 삭제 (soft delete)
     * DELETE /api/v1/orders/{orderId}
     * MASTER만 가능
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId,
            Authentication authentication
    ) {
        UserRoleEnum role = extractRole(authentication);
        orderService.deleteOrder(orderId, userId, role);
        return ResponseEntity.noContent().build();
    }

    /**
     * 페이지 사이즈 10/30/50만 허용
     */
    private void validatePageSize(int pageSize) {
        if (pageSize != 10 && pageSize != 30 && pageSize != 50) {
            throw new BaseException(CommonErrorCode.INVALID_PAGE_SIZE);
        }
    }

    /**
     * Authentication에서 UserRoleEnum 추출
     */
    private UserRoleEnum extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> UserRoleEnum.valueOf(a.getAuthority().replace("ROLE_", "")))
                .orElseThrow(() -> new BaseException(CommonErrorCode.FORBIDDEN));
    }
}