package com.sparta.todayeats.payment.controller;

import com.sparta.todayeats.category.presentation.dto.PageResponse;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.payment.dto.request.PaymentCreateRequest;
import com.sparta.todayeats.payment.dto.response.PaymentCreateResponse;
import com.sparta.todayeats.payment.service.PaymentService;
import com.sparta.todayeats.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders/{orderId}/payments")
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> createPayment(
            @PathVariable("orderId") UUID orderId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody PaymentCreateRequest request) {
        PaymentCreateResponse response = paymentService.createPayment(orderId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments(@PathVariable("orderId") UUID orderId) {

    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable("paymentId") UUID paymentId) {

    }

    @PutMapping("/payments/{paymentId}")
    public ResponseEntity<Payment> updatePayment(@PathVariable("paymentId") UUID paymentId, @RequestBody Payment payment) {

    }

    @DeleteMapping("/payments/{paymentId}")
    public ResponseEntity<Payment> deletePayment(@PathVariable("paymentId") UUID paymentId) {

    }

}
