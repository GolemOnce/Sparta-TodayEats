package com.sparta.todayeats.menu.presentation.dto.response;

import com.sparta.todayeats.menu.domain.entity.MenuEntity;

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

    public static MenuResponse from(MenuEntity menu) {
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