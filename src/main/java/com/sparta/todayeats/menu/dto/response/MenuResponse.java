package com.sparta.todayeats.menu.dto.response;

import com.sparta.todayeats.menu.entity.Menu;

import java.util.UUID;

public record MenuResponse(
        UUID id,
        String name,
        int price,
        String description,
        boolean isHidden,
        boolean soldOut,
        String imageUrl,
        UUID categoryId,
        UUID storeId
) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.isHidden(),
                menu.isSoldOut(),
                menu.getImageUrl(),
                menu.getCategory().getId(),
                menu.getStore().getId()
        );
    }
}