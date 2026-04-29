package com.sparta.todayeats.menu.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.todayeats.menu.entity.Menu;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class MenuCreateResponse {

    @JsonProperty("menu_id")
    private UUID menuId;

    @JsonProperty("store_id")
    private UUID storeId;

    private String name;
    private long price;

    @JsonProperty("sold_out")
    private boolean soldOut;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("created_by")
    private String createdBy;

    public static MenuCreateResponse from(Menu menu) {
        return MenuCreateResponse.builder()
                .menuId(menu.getId())
                .storeId(menu.getStore().getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .soldOut(menu.isSoldOut())
                .createdAt(menu.getCreatedAt())
                .createdBy(String.valueOf(menu.getCreatedBy()))
                .build();
    }
}
