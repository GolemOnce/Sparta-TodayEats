package com.sparta.todayeats.order.domain.entity;

import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.OrderErrorCode;
import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends BaseEntity {

    private static final int CANCEL_LIMIT_MINUTES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", updatable = false, nullable = false)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;                    // FK → p_user.user_id

    @Column(name = "store_id", nullable = false)
    private UUID storeId;                       // FK → p_store.store_id

    @Column(name = "address_id", nullable = false)
    private UUID addressId;                     // FK → p_address.address_id

    // ─── 스냅샷 필드 (주문 시점 값 고정) ────────────────────────────────
    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;                   // 가게명 스냅샷

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;             // 도로명 주소 스냅샷

    @Column(name = "delivery_detail", length = 255)
    private String deliveryDetail;              // 상세주소 스냅샷
    // ─────────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @Builder
    public OrderEntity(UUID customerId, UUID storeId, UUID addressId,
                       String storeName, String deliveryAddress, String deliveryDetail,
                       OrderType orderType, String note, Long totalPrice) {
        this.customerId = customerId;
        this.storeId = storeId;
        this.addressId = addressId;
        this.storeName = storeName;
        this.deliveryAddress = deliveryAddress;
        this.deliveryDetail = deliveryDetail;
        this.orderType = orderType;
        this.status = OrderStatus.PENDING;  // 최초 생성 시 항상 PENDING
        this.note = note;
        this.totalPrice = totalPrice;
    }

    // 주문 항목 추가
    public void addOrderItem(OrderItemEntity item) {
        this.orderItems.add(item);
    }

    // totalPrice 갱신 (서버에서 계산 후 세팅)
    public void updateTotalPrice(long total) {
        this.totalPrice = total;
    }

    /**
     * 요청사항 수정
     * - PENDING 상태만 수정 가능
     * TODO: JWT 완성 후 주석 해제
     * - CUSTOMER 본인만 수정 가능
     */
    public void updateNote(String note) {
        if (this.status != OrderStatus.PENDING) {
            throw new BaseException(OrderErrorCode.ORDER_UPDATE_NOT_ALLOWED);
        }
        this.note = note;

        // TODO: JWT 완성 후 주석 해제
        // if (!this.customerId.equals(userId)) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }
    }

    /**
     * 주문 상태 전이 (OWNER/MANAGER/MASTER 액션)
     * OrderStatus.validateTransition()으로 허용된 전이만 가능
     * TODO: JWT 완성 후 주석 해제
     * - OWNER: 본인 가게 주문만 변경 가능
     * - MANAGER/MASTER: 전체 변경 가능
     */
    public void changeStatus(OrderStatus nextStatus) {
        this.status.validateTransition(nextStatus);
        this.status = nextStatus;

        // TODO: JWT 완성 후 주석 해제
        // if (role == UserRole.OWNER) {
        //     StoreEntity store = storeRepository.findActiveById(this.storeId)
        //             .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
        //     if (!store.getOwnerId().equals(userId)) {
        //         throw new BaseException(CommonErrorCode.FORBIDDEN);
        //     }
        // }
    }

    /**
     * 주문 취소 (CUSTOMER 본인 / MASTER만 가능)
     * 조건 1: PENDING 상태여야 함
     * 조건 2: 주문 생성 후 5분 이내여야 함
     * TODO: JWT 완성 후 주석 해제
     * - CUSTOMER 본인 또는 MASTER만 가능
     */
    public void cancelByCustomer() {
        if (this.status != OrderStatus.PENDING) {
            throw new BaseException(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED);
        }
        LocalDateTime cancelDeadline = this.getCreatedAt().plusMinutes(CANCEL_LIMIT_MINUTES);
        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new BaseException(OrderErrorCode.CANCEL_TIME_EXCEEDED);
        }
        this.status = OrderStatus.CANCELED;

        // TODO: JWT 완성 후 주석 해제
        // if (role != UserRole.CUSTOMER && role != UserRole.MASTER) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }
        // if (role == UserRole.CUSTOMER && !this.customerId.equals(userId)) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }
    }

    /**
     * Soft delete (MASTER만 가능)
     * BaseEntity.softDelete(UUID) 사용
     * TODO: JWT 완성 후 주석 해제
     * - MASTER만 삭제 가능
     */
    public void delete(UUID userId) {
        this.softDelete(userId);

        // TODO: JWT 완성 후 주석 해제
        // if (role != UserRole.MASTER) {
        //     throw new BaseException(CommonErrorCode.FORBIDDEN);
        // }
    }
}