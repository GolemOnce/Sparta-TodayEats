package com.sparta.todayeats.menu.presentation.dto.request;

public record MenuUpdateRequest(
        String name,
        int price,
        String description,
        String imageUrl
) {
}