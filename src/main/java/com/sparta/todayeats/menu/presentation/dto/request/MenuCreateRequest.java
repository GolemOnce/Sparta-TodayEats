package com.sparta.todayeats.menu.presentation.dto.request;

import java.util.UUID;

// 카테고리 id와 스토어 id를
// Entity에서 직접 받지 않고 Service에서 조회
public record MenuCreateRequest(
        String name,
        int price,
        String description,
        String imageUrl,
        UUID categoryId
) {
}