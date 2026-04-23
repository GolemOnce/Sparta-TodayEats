package com.sparta.todayeats.menu.domain.entity;

import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import java.util.UUID;

@Entity
@Table(name = "p_menu")
@Getter
public class MenuEntity extends BaseEntity {

    @Id
    @Column(name = "menu_id")
    private UUID menuId;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Integer price;

    @Column(name = "is_hidden")
    private Boolean isHidden;   // 숨김 여부
}