package com.sparta.todayeats.order.repository;

import com.sparta.todayeats.order.entity.Order;
import jakarta.persistence.LockModeType;
import com.sparta.todayeats.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // 비관적 락 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.orderId = :id")
    Optional<Order> findByIdWithLock(@Param("id") UUID id);

    /**
     * soft delete 제외 단건 조회
     *
     * @param orderId 조회할 주문 ID
     * @return 삭제되지 않은 주문 Optional
     */
    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId AND o.deletedAt IS NULL")
    Optional<Order> findActiveById(@Param("orderId") UUID orderId);

    /**
     * OWNER 전용: 본인 가게 주문 검색 (status, storeName 필터 포함)
     *
     * @param ownerId   가게 소유자 ID
     * @param status    주문 상태 필터 (null이면 전체)
     * @param storeName 가게명 필터 (null이면 전체, 부분 일치)
     * @param pageable  페이지 정보
     * @return 조건에 맞는 해당 소유자 가게의 주문 페이지
     */
    @Query("SELECT o FROM Order o " +
            "JOIN Store s ON o.storeId = s.id " +
            "WHERE s.owner.userId = :ownerId " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:storeName IS NULL OR o.storeName LIKE %:storeName%) " +
            "AND o.deletedAt IS NULL")
    Page<Order> searchOrdersByStoreOwner(
            @Param("ownerId") UUID ownerId,
            @Param("status") OrderStatus status,
            @Param("storeName") String storeName,
            Pageable pageable);

    /**
     * CUSTOMER/OWNER 전용: 검색 조건 + 페이지네이션으로 주문 조회 (soft delete 제외)
     *
     * @param customerId 고객 ID
     * @param status     주문 상태 필터 (null이면 전체)
     * @param storeName  가게명 필터 (null이면 전체, 부분 일치)
     * @param pageable   페이지 정보
     * @return 조건에 맞는 주문 페이지
     */
    @Query("SELECT o FROM Order o " +
            "WHERE o.customerId = :customerId " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:storeName IS NULL OR o.storeName LIKE %:storeName%) " +
            "AND o.deletedAt IS NULL")
    Page<Order> searchOrders(
            @Param("customerId") UUID customerId,
            @Param("status") OrderStatus status,
            @Param("storeName") String storeName,
            Pageable pageable);

    /**
     * MANAGER 전용: 전체 주문 검색 (soft delete 제외)
     *
     * @param status    주문 상태 필터 (null이면 전체)
     * @param storeName 가게명 필터 (null이면 전체, 부분 일치)
     * @param pageable  페이지 정보
     * @return 조건에 맞는 전체 주문 페이지
     */
    @Query("SELECT o FROM Order o " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:storeName IS NULL OR o.storeName LIKE %:storeName%) " +
            "AND o.deletedAt IS NULL")
    Page<Order> searchAllOrders(
            @Param("status") OrderStatus status,
            @Param("storeName") String storeName,
            Pageable pageable);

    /**
     * MASTER 전용: 삭제된 주문 포함 전체 검색
     *
     * @param status    주문 상태 필터 (null이면 전체)
     * @param storeName 가게명 필터 (null이면 전체, 부분 일치)
     * @param pageable  페이지 정보
     * @return 삭제 여부 관계없이 조건에 맞는 전체 주문 페이지
     */
    @Query("SELECT o FROM Order o " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:storeName IS NULL OR o.storeName LIKE %:storeName%)")
    Page<Order> searchAllOrdersIncludeDeleted(
            @Param("status") OrderStatus status,
            @Param("storeName") String storeName,
            Pageable pageable);
}
