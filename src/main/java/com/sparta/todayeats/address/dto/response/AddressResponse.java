package com.sparta.todayeats.address.dto.response;

import com.sparta.todayeats.address.entity.Address;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AddressResponse {
    private UUID addressId;
    private UUID userId;
    private String alias;
    private String address;
    private String detail;
    private String zipCode;
    private boolean isDefault;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    public static AddressResponse from(Address address) {
        return AddressResponse.builder()
                .addressId(address.getId())
                .userId(address.getUser().getUserId())
                .alias(address.getAlias())
                .address(address.getAddress())
                .detail(address.getDetail())
                .zipCode(address.getZipCode())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .createdBy(address.getCreatedBy())
                .updatedAt(address.getUpdatedAt())
                .updatedBy(address.getUpdatedBy())
                .build();
    }
}
