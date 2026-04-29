package com.sparta.todayeats.store.entity;

import com.sparta.todayeats.area.domain.entity.Area;
import com.sparta.todayeats.category.entity.Category;
import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import com.sparta.todayeats.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at is null")
@Table(
        name = "p_store",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "deleted_at"})
)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "store_id")
    private UUID id;

    // username FK (VARCHAR(10))
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    // DECIMAL(2,1) - ex) 4.5
    @Column(nullable = false, precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    // softDelete랑 별도 - 가게 숨김 처리
    @Column(nullable = false)
    @Builder.Default
    private Boolean isHidden = false;

    public void update(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public void updateAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    // 숨김 처리 (isHidden 토글)
    public void hide() {
        this.isHidden = true;
    }

    public void show() {
        this.isHidden = false;
    }
}
