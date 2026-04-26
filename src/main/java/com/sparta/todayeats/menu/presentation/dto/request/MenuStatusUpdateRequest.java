package com.sparta.todayeats.menu.presentation.dto.request;

public record MenuStatusUpdateRequest(
        boolean isHidden,
        boolean soldOut
) {
}