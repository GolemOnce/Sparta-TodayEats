package com.sparta.todayeats.menu.dto.request;

public record MenuStatusUpdateRequest(
        boolean isHidden,
        boolean soldOut
) {
}