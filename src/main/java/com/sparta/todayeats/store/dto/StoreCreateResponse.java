package com.sparta.todayeats.store.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// 가게 생성 응답
@Getter
@Builder
public class StoreCreateResponse {
    private UUID storeId;
    private String ownerNickname;
    private String name;
    private String address;
    private String phone;
    private UUID areaId;
    private String areaName;
    private UUID categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private UUID createdBy;
}
