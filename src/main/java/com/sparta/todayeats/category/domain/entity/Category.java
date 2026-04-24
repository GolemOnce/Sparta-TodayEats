package com.sparta.todayeats.category.domain.entity;

import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table( name = "p_category")
@Getter
@Builder
@AllArgsConstructor
@Where(clause = "deleted_at is null")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    public void updateName(String name) {
        this.name = name;
    }

}
