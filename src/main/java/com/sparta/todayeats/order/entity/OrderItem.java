package com.sparta.todayeats.order.entity;

import com.sparta.todayeats.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 주문 항목 엔티티
 * 주문 시점의 메뉴명·단가를 스냅샷으로 저장한다.
 */
@Entity
@Table(name = "p_order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_id", updatable = false, nullable = false)
    private UUID orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // ─── 주문 시점 스냅샷 (메뉴 삭제/변경되어도 주문 내역 보존) ──────────
    @Column(name = "menu_id", nullable = false)
    private UUID menuId;                    // FK (참조용)

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;               // 메뉴명 스냅샷

    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;             // 단가 스냅샷
    // ─────────────────────────────────────────────────────────────────────

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Builder
    public OrderItem(Order order, UUID menuId, String menuName,
                     Long unitPrice, Integer quantity) {
        this.order = order;
        this.menuId = menuId;
        this.menuName = menuName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
}