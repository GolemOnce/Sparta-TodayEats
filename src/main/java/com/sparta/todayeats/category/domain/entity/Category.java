package com.sparta.todayeats.category.domain.entity;

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
        name = "p_category",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "deleted_at"})
) // 삭제되지 않은 카테고리 이름 중복 방지
public class Category extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    public void updateName(String name) {
        this.name = name;
    }

}
