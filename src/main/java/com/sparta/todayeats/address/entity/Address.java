package com.sparta.todayeats.address.entity;

import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import java.util.UUID;

@Entity
@Table(name = "p_address")
@Getter
public class Address extends BaseEntity {

    @Id
    @Column(name = "address_id")
    private UUID addressId;

    @Column(name = "address")
    private String address;

    @Column(name = "detail")
    private String detail;

    @Column(name = "user_id")
    private UUID userId;
}