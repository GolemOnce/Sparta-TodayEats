package com.sparta.todayeats.payment.repository;

import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    // 목록 조회
    @Query(
            value = "SELECT p FROM Payment p JOIN FETCH p.order o WHERE o.customerId = :userId AND p.deletedAt IS NULL",
            countQuery = "SELECT COUNT(p) FROM Payment p JOIN p.order o WHERE o.customerId = :userId AND p.deletedAt IS NULL"
    )
    Page<Payment> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    // 단일 조회
    Optional<Payment> findByIdAndOrder_CustomerIdAndDeletedAtIsNull(UUID paymentId, UUID customerId);
}
