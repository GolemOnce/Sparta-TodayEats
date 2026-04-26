package com.sparta.todayeats.payment.entity;

import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import com.sparta.todayeats.order.entity.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_payment")
@Getter
@Builder
@AllArgsConstructor
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
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotNull
    private Long amount;

    public void updatePaymentStatus(PaymentStatus status) {
        this.status = status;
    }
}
