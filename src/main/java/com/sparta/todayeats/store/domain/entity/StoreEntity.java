package com.sparta.todayeats.store.domain.entity;

import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import java.util.UUID;

@Entity
@Table(name = "p_store")
@Getter
public class StoreEntity extends BaseEntity {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "name")
    private String name;

    @Column(name = "owner_id")
    private UUID ownerId;           // 가게 소유자 FK → p_user.user_id

    @Column(name = "is_hidden")
    private Boolean isHidden;       // 가게 숨김 여부

    @Column(name = "category_id")
    private UUID categoryId;        // 카테고리 FK

    @Column(name = "area_id")
    private UUID areaId;            // 운영 지역 FK
}