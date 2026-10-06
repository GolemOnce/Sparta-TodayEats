package com.sparta.todayeats.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "공통 Page 응답 객체")
@Getter
@Builder
public class PageResponse<T> {
    @Schema(description = "데이터 목록")
    private List<T> content;

    @Schema(description = "현재 페이지 번호", example = "0")
    private int page;

    @Schema(description = "페이지 당 데이터 개수", example = "20")
    private int size;

    @Schema(description = "전체 데이터 개수", example = "10")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "1")
    private int totalPages;

    @Schema(description = "정렬 기준", example = "createdAt,desc")
    private String sort;

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