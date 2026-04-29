package com.sparta.todayeats.global.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

// 목록 API의 page 응답 DTO
@Getter
@Builder
public class PageResponse<T> {
    private List<T> content;    // 데이터 목록
    private int page;           // 현재 페이지 번호
    private int size;           // 페이지 당 데이터 개수
    private long totalElements; // 전체 데이터 개수
    private int totalPages;     // 전체 페이지 수
    private String sort;        // 정렬 기준

    // Page를 PageResponse로 변환
    public static <T, E> PageResponse<T> from(Page<E> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .sort(page.getSort().toString())
                .build();
    }
}