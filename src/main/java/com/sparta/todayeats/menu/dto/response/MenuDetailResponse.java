package com.sparta.todayeats.menu.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.todayeats.menu.entity.Menu;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class MenuDetailResponse {

    @JsonProperty("menu_id")
    private UUID menuId;

    @JsonProperty("store_id")
    private UUID storeId;

    private String name;
    private String description;   // 추가

    private int price;

    @JsonProperty("is_sold_out")
    private boolean soldOut;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("updated_by")
    private String updatedBy;

    public static MenuDetailResponse from(Menu menu) {
        return MenuDetailResponse.builder()
                .menuId(menu.getId())
                .storeId(menu.getStore().getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(Math.toIntExact(menu.getPrice()))
                .soldOut(menu.isSoldOut())
                .createdAt(menu.getCreatedAt())
                .createdBy(String.valueOf(menu.getCreatedBy()))
                .updatedAt(menu.getUpdatedAt())
                .updatedBy(String.valueOf(menu.getUpdatedBy()))
                .build();
    }
}