package com.sparta.todayeats.order.domain.repository;

import com.sparta.todayeats.order.domain.entity.OrderEntity;
import com.sparta.todayeats.order.domain.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // OWNER: 본인 가게 주문 목록 (soft delete 제외)
    // TODO: JWT 완성 후 사용
    @Query("SELECT o FROM OrderEntity o " +
            "JOIN StoreEntity s ON o.storeId = s.storeId " +
            "WHERE s.ownerId = :ownerId AND o.deletedAt IS NULL")
    Page<OrderEntity> findAllByStoreOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    // MANAGER/MASTER: 전체 주문 목록 (soft delete 제외)
    // TODO: JWT 완성 후 사용
    @Query("SELECT o FROM OrderEntity o WHERE o.deletedAt IS NULL")
    Page<OrderEntity> findAllActive(Pageable pageable);

    // 가게별 주문 목록 (soft delete 제외, 페이지네이션)
    @Query("SELECT o FROM OrderEntity o WHERE o.storeId = :storeId AND o.deletedAt IS NULL")
    Page<OrderEntity> findAllByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    // CUSTOMER/OWNER: 검색 조건 + 페이지네이션 (soft delete 제외)
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.customerId = :customerId " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:storeName IS NULL OR o.storeName LIKE %:storeName%) " +
            "AND o.deletedAt IS NULL")
    Page<OrderEntity> searchOrders(
            @Param("customerId") UUID customerId,
            @Param("status") OrderStatus status,
            @Param("storeName") String storeName,
            Pageable pageable);

    // MANAGER/MASTER: 전체 검색 (soft delete 제외)
    // TODO: JWT 완성 후 사용
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:storeName IS NULL OR o.storeName LIKE %:storeName%) " +
            "AND o.deletedAt IS NULL")
    Page<OrderEntity> searchAllOrders(
            @Param("status") OrderStatus status,
            @Param("storeName") String storeName,
            Pageable pageable);

    // MASTER 전용: 삭제된 주문 포함 전체 검색
    // TODO: JWT 완성 후 사용
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:storeName IS NULL OR o.storeName LIKE %:storeName%)")
    Page<OrderEntity> searchAllOrdersIncludeDeleted(
            @Param("status") OrderStatus status,
            @Param("storeName") String storeName,
            Pageable pageable);

    // 상태 전이 조건부 UPDATE (currentStatus일 때만 nextStatus로 변경)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE OrderEntity o SET o.status = :nextStatus " +
            "WHERE o.orderId = :orderId AND o.status = :currentStatus AND o.deletedAt IS NULL")
    int updateStatusConditionally(@Param("orderId") UUID orderId,
                                  @Param("currentStatus") OrderStatus currentStatus,
                                  @Param("nextStatus") OrderStatus nextStatus);

    // 취소 조건부 UPDATE (PENDING일 때만)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE OrderEntity o SET o.status = :nextStatus, o.cancelReason = :cancelReason " +
            "WHERE o.orderId = :orderId AND o.status = :currentStatus AND o.deletedAt IS NULL")
    int cancelConditionally(@Param("orderId") UUID orderId,
                            @Param("cancelReason") String cancelReason,
                            @Param("currentStatus") OrderStatus currentStatus,
                            @Param("nextStatus") OrderStatus nextStatus);

    // 거절 조건부 UPDATE (PENDING일 때만)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE OrderEntity o SET o.status = :nextStatus, o.rejectReason = :rejectReason " +
            "WHERE o.orderId = :orderId AND o.status = :currentStatus AND o.deletedAt IS NULL")
    int rejectConditionally(@Param("orderId") UUID orderId,
                            @Param("rejectReason") String rejectReason,
                            @Param("currentStatus") OrderStatus currentStatus,
                            @Param("nextStatus") OrderStatus nextStatus);

}