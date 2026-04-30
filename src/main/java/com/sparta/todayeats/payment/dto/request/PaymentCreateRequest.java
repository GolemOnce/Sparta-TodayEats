package com.sparta.todayeats.payment.dto.request;


import com.sparta.todayeats.payment.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "결제 생성 요청")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreateRequest {
    @Schema(description = "결제 수단", example = "CARD")
    @NotNull
    private PaymentMethod paymentMethod;
}
