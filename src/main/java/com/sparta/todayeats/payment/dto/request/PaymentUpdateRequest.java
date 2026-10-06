package com.sparta.todayeats.payment.dto.request;

import com.sparta.todayeats.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "결제 수정 요청")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentUpdateRequest {
    @Schema(description = "결제 상태", example = "COMPLETED")
    @NotNull(message = "변경할 상태를 입력해주세요.")
    PaymentStatus status;
}
