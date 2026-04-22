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
}