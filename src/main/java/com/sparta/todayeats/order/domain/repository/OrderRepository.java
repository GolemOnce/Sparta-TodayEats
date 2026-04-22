package com.sparta.todayeats.order.domain.repository;

import com.sparta.todayeats.order.domain.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    // soft delete 제외 단건 조회
    @Query("SELECT o FROM OrderEntity o WHERE o.orderId = :orderId AND o.deletedAt IS NULL")
    Optional<OrderEntity> findActiveById(@Param("orderId") UUID orderId);

    // 사용자별 주문 목록 (soft delete 제외, 페이지네이션)
    @Query("SELECT o FROM OrderEntity o WHERE o.customerId = :customerId AND o.deletedAt IS NULL")
    Page<OrderEntity> findAllByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

    // 가게별 주문 목록 (soft delete 제외, 페이지네이션)
    @Query("SELECT o FROM OrderEntity o WHERE o.storeId = :storeId AND o.deletedAt IS NULL")
    Page<OrderEntity> findAllByStoreId(@Param("storeId") UUID storeId, Pageable pageable);
}