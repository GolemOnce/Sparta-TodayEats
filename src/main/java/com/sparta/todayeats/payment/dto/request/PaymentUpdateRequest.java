package com.sparta.todayeats.payment.dto.request;

import com.sparta.todayeats.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentUpdateRequest {
    @NotNull(message = "변경할 상태를 입력해주세요.")
    PaymentStatus status;
}
