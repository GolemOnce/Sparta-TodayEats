package com.sparta.todayeats.address.entity;

import com.sparta.todayeats.address.dto.request.AddressUpdateRequest;
import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import com.sparta.todayeats.user.entity.User;
import jakarta.persistence.*;

import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Builder
@Table(name = "p_address")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String alias;

    @Column(nullable = false)
    private String address;

    private String detail;

    private String zipCode;

    private boolean isDefault;

    public void updateAddress(AddressUpdateRequest request) {
        this.alias = request.getAlias();
        this.address = request.getAddress();
        this.detail = request.getDetail();
        this.zipCode = request.getZipCode();
    }

    public void updateDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
