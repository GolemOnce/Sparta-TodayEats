package com.sparta.todayeats.payment.controller;

import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.payment.dto.request.PaymentCreateRequest;
import com.sparta.todayeats.payment.dto.request.PaymentUpdateRequest;
import com.sparta.todayeats.payment.dto.response.*;
import com.sparta.todayeats.payment.entity.Payment;
import com.sparta.todayeats.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 처리
    @PostMapping("/orders/{orderId}/payments")
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> createPayment(
            @PathVariable("orderId") UUID orderId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody PaymentCreateRequest request) {
        PaymentCreateResponse response = paymentService.createPayment(orderId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // 결제 목록 조회
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentPageResponse>> getPayments(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(name = "userId", required = false) UUID targetUserId,
            Pageable pageable) {
        PaymentPageResponse response = paymentService.getPagedPayments(userId, targetUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 결제 상세 조회
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPayment(
            @PathVariable("paymentId") UUID paymentId,
            @AuthenticationPrincipal UUID userId) {
        PaymentDetailResponse response = paymentService.getPaymentDetails(userId, paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 결제 상태 변경
    @PutMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentUpdateResponse>> updatePayment(
            @PathVariable("paymentId") UUID paymentId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody PaymentUpdateRequest request) {
        PaymentUpdateResponse response = paymentService.changePaymentStatus(paymentId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 결제 삭제
    @DeleteMapping("/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable("paymentId") UUID paymentId,
            @AuthenticationPrincipal UUID userId) {
        paymentService.deletePayment(paymentId, userId);
        return ResponseEntity.noContent().build();
    }

}
