package com.sparta.todayeats.store.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// 가게 숨김 응답 DTO
@Getter
@Builder
public class StoreHiddenResponse {
    private UUID storeId;
    private Boolean isHidden;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;
}
