package com.sparta.todayeats.payment.dto.response;

import com.sparta.todayeats.payment.entity.Payment;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PaymentPageResponse {
    private List<PaymentResponse> payments;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;

    public static PaymentPageResponse from(Page<Payment> paymentPage) {
        return PaymentPageResponse.builder()
                .payments(paymentPage.getContent().stream()
                        .map(PaymentResponse::from)
                        .toList())
                .page(paymentPage.getNumber())
                .size(paymentPage.getSize())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .sort("createdAt,DESC")
                .build();
    }
}
