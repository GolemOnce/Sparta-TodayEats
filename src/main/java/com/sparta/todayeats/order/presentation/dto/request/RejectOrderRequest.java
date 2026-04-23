package com.sparta.todayeats.order.presentation.dto.request;

import jakarta.validation.constraints.Size;

public record RejectOrderRequest(

        @Size(max = 255, message = "거절 사유는 255자 이하여야 합니다.")
        String rejectReason  // 거절 사유 (선택)
) {}