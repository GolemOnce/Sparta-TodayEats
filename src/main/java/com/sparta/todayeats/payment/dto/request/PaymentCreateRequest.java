package com.sparta.todayeats.payment.dto.request;


import com.sparta.todayeats.payment.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreateRequest {
    private PaymentMethod paymentMethod;
}
