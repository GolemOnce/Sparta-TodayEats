package com.sparta.todayeats.payment.dto.response;

import com.sparta.todayeats.payment.entity.Payment;
import com.sparta.todayeats.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "결제 상태 변경 응답")
@Getter
@Builder
public class PaymentUpdateResponse {
    @Schema(description = "결제 ID", example = "p10e8400-e29b-41d4-a716-446655449999")
    private UUID paymentId;

    @Schema(description = "결제 상태", example = "CANCELED")
    private PaymentStatus status;

    @Schema(description = "생성 시간", example = "2026-04-30T15:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655441111")
    private UUID createdBy;

    @Schema(description = "수정 시간", example = "2026-04-30T15:10:00")
    private LocalDateTime updatedAt;

    @Schema(description = "수정자 ID", example = "990e8400-e29b-41d4-a716-446655449999")
    private UUID updatedBy;

    public static PaymentUpdateResponse from(Payment payment) {
        return PaymentUpdateResponse.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .createdBy(payment.getCreatedBy())
                .updatedAt(payment.getUpdatedAt())
                .updatedBy(payment.getUpdatedBy())
                .build();
    }
}
