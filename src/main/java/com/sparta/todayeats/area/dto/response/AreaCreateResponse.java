package com.sparta.todayeats.area.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// 운영 지역 생성 응답 DTO
@Getter
@Builder
public class AreaCreateResponse {
    private UUID areaId;
    private String name;
    private String city;
    private String district;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private UUID createdBy;
}
