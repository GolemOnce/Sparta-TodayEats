package com.sparta.todayeats.category.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 목록 API의 page 응답 DTO
@Getter
@Builder
public class PageResponse<T> {

    private List<T> content;

    private int page;
    private int size;

    private long totalElements;
    private int totalPages;

    private String sort;
}
