package com.sparta.todayeats.address.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AddressCreateResponse {
    private UUID addressId;
    private UUID userId;
    private String alias;
    private String address;
    private String detail;
    private String zipCode;
    private boolean isDefault;
    private LocalDateTime createdAt;
    private UUID createdBy;
}
