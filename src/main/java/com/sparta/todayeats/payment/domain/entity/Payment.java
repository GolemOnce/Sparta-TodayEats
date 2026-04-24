package com.sparta.todayeats.payment.domain.entity;

import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import com.sparta.todayeats.order.domain.entity.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotNull
    private Long amount;

    public static Payment create(Order order, Long amount) {
        Payment payment = new Payment();
        payment.order = order;
        payment.amount = amount;
        return  payment;
    }
}
