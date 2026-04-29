package com.sparta.todayeats.area.domain.entity;

import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Where(clause = "deleted_at is null")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "p_area",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "deleted_at"})
)
public class Area extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "area_id")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 50)
    private String district;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public void update(String name, String city, String district, Boolean isActive) {
        this.name = name;
        this.city = city;
        this.district = district;

        if (isActive != null) {
            this.isActive = isActive;
        }
    }
}
