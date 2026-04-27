package com.sparta.todayeats.order.repository;

import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/** 주문 레포지토리 */
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * soft delete 제외 단건 조회
     *
     * @param orderId 조회할 주문 ID
     * @return 삭제되지 않은 주문 Optional
     */
    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId AND o.deletedAt IS NULL")
    Optional<Order> findActiveById(@Param("orderId") UUID orderId);

    /**
     * 사용자별 주문 목록 조회 (soft delete 제외, 페이지네이션)
     *
     * @param customerId 조회할 고객 ID
     * @param pageable   페이지 정보
     * @return 삭제되지 않은 해당 고객의 주문 페이지
     */
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.deletedAt IS NULL")
    Page<Order> findAllByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

    /**
     * OWNER 전용: 본인 가게 주문 목록 조회 (soft delete 제외)
     * TODO: JWT 완성 후 사용
     *
     * @param ownerId  가게 소유자 ID
     * @param pageable 페이지 정보
     * @return 해당 소유자의 가게에 속한 주문 페이지
     */
    @Query("SELECT o FROM Order o " +
            "JOIN Store s ON o.storeId = s.id " +
            "WHERE s.owner.userId = :ownerId AND o.deletedAt IS NULL")
    Page<Order> findAllByStoreOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    /**
     * MANAGER/MASTER 전용: 전체 주문 목록 조회 (soft delete 제외)
     * TODO: JWT 완성 후 사용
     *
     * @param pageable 페이지 정보
     * @return 삭제되지 않은 전체 주문 페이지
     */
    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL")
    Page<Order> findAllActive(Pageable pageable);

    /**
     * 가게별 주문 목록 조회 (soft delete 제외, 페이지네이션)
     *
     * @param storeId  조회할 가게 ID
     * @param pageable 페이지 정보
     * @return 해당 가게의 삭제되지 않은 주문 페이지
     */
    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId AND o.deletedAt IS NULL")
    Page<Order> findAllByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

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
     * MANAGER/MASTER 전용: 전체 주문 검색 (soft delete 제외)
     * TODO: JWT 완성 후 사용
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
     * TODO: JWT 완성 후 사용
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

    /**
     * 상태 전이 조건부 UPDATE.
     * currentStatus 일 때만 nextStatus 로 변경하며, soft delete된 주문은 제외합니다.
     *
     * @param orderId       상태를 변경할 주문 ID
     * @param currentStatus 현재 주문 상태 (이 상태일 때만 변경)
     * @param nextStatus    변경할 다음 주문 상태
     * @return 업데이트된 행 수 (0이면 조건 불일치)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.status = :nextStatus, o.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE o.orderId = :orderId AND o.status = :currentStatus AND o.deletedAt IS NULL")
    int updateStatusConditionally(@Param("orderId") UUID orderId,
                                  @Param("currentStatus") OrderStatus currentStatus,
                                  @Param("nextStatus") OrderStatus nextStatus);

    /**
     * 취소 조건부 UPDATE.
     * PENDING 상태이며 생성 후 5분 이내인 주문만 취소 처리합니다.
     * JPQL은 DB 함수 기반 날짜 연산을 표준으로 지원하지 않아 PostgreSQL native query를 사용합니다.
     * 5분 제한을 상태 변경과 원자적으로 처리하기 위해 native query를 사용합니다.
     *
     * @param orderId      취소할 주문 ID
     * @param cancelReason 취소 사유
     * @param currentStatus 현재 주문 상태 문자열 (PENDING)
     * @param nextStatus   변경할 상태 문자열 (CANCELLED)
     * @return 업데이트된 행 수 (0이면 조건 불일치 또는 5분 초과)
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE p_order SET status = :nextStatus, cancel_reason = :cancelReason, updated_at = NOW() " +
            "WHERE order_id = :orderId AND status = :currentStatus " +
            "AND created_at >= NOW() - INTERVAL '5 minutes' " +
            "AND deleted_at IS NULL", nativeQuery = true)
    int cancelConditionally(@Param("orderId") UUID orderId,
                            @Param("cancelReason") String cancelReason,
                            @Param("currentStatus") String currentStatus,
                            @Param("nextStatus") String nextStatus);

    /**
     * 거절 조건부 UPDATE.
     * PENDING 상태일 때만 거절 처리하며, soft delete된 주문은 제외합니다.
     *
     * @param orderId      거절할 주문 ID
     * @param rejectReason 거절 사유
     * @param currentStatus 현재 주문 상태 (PENDING이어야 함)
     * @param nextStatus   변경할 다음 상태 (REJECTED)
     * @return 업데이트된 행 수 (0이면 조건 불일치)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.status = :nextStatus, o.rejectReason = :rejectReason, o.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE o.orderId = :orderId AND o.status = :currentStatus AND o.deletedAt IS NULL")
    int rejectConditionally(@Param("orderId") UUID orderId,
                            @Param("rejectReason") String rejectReason,
                            @Param("currentStatus") OrderStatus currentStatus,
                            @Param("nextStatus") OrderStatus nextStatus);

}