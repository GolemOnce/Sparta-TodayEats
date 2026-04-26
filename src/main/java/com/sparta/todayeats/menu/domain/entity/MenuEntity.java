package com.sparta.todayeats.menu.domain.entity;

import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import com.sparta.todayeats.category.domain.entity.Category;
import com.sparta.todayeats.store.domain.entity.StoreEntity;

import java.util.UUID;

@Entity
@Table(name = "p_menu")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    // Id
    @Column(name = "menu_id", updatable = false, nullable = false)
    private UUID id;

    // Name
    @Column(nullable = false, length = 100)
    private String name;

    // Price
    @Column(nullable = false)
    private int price;

    // Description
    @Column(length = 500)
    private String description;

    // Hidden
    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden;

    // Sold Out
    @Column(name = "sold_out", nullable = false)
    private boolean soldOut;

    // Image URL
    @Column(length = 255)
    private String imageUrl;

    // Soft Delete
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    // 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 스토어
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    // 메서드
    public void assignCategory(Category category) {
        this.category = category;
    }

    public void assignStore(StoreEntity store) {
        this.store = store;
    }

    public void update(String name, int price, String description, String imageUrl) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public void updateStatus(boolean isHidden, boolean soldOut) {
        this.isHidden = isHidden;
        this.soldOut = soldOut;
    }

    public void hide() {
        this.isHidden = true;
    }

    public void show() {
        this.isHidden = false;
    }

    public void delete() {
        this.isDeleted = true;
    }
}