package com.sparta.todayeats.payment.controller;

import com.sparta.todayeats.global.annotation.ApiNoContent;
import com.sparta.todayeats.global.annotation.ApiPageable;
import com.sparta.todayeats.global.annotation.LoginUser;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.payment.dto.request.PaymentCreateRequest;
import com.sparta.todayeats.payment.dto.request.PaymentUpdateRequest;
import com.sparta.todayeats.payment.dto.response.*;
import com.sparta.todayeats.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Payment")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 처리")
    @PostMapping("/orders/{orderId}/payments")
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> createPayment(
            @Parameter(description = "주문 ID", example = "770e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID orderId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Valid @RequestBody PaymentCreateRequest request) {
        PaymentCreateResponse response = paymentService.createPayment(orderId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "결제 목록 조회")
    @ApiPageable
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPayments(
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Parameter(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(name = "userId", required = false) UUID targetUserId,
            @Parameter(hidden = true) Pageable pageable
    ) {
        PageResponse<PaymentResponse> response = paymentService.getPagedPayments(userId, targetUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "결제 상세 조회")
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPayment(
            @Parameter(description = "결제 ID", example = "p10e8400-e29b-41d4-a716-446655449999")
            @PathVariable UUID paymentId,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {
        PaymentDetailResponse response = paymentService.getPaymentDetails(userId, paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "결제 상태 변경")
    @PutMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentUpdateResponse>> updatePayment(
            @Parameter(description = "결제 ID", example = "p10e8400-e29b-41d4-a716-446655449999")
            @PathVariable UUID paymentId,
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Valid @RequestBody PaymentUpdateRequest request
    ) {
        PaymentUpdateResponse response = paymentService.changePaymentStatus(paymentId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "결제 삭제")
    @ApiNoContent
    @DeleteMapping("/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(
            @Parameter(description = "결제 ID", example = "p10e8400-e29b-41d4-a716-446655449999")
            @PathVariable UUID paymentId,
            @Parameter(hidden = true) @LoginUser UUID userId)
    {
        paymentService.deletePayment(paymentId, userId);
        return ResponseEntity.noContent().build();
    }

}
