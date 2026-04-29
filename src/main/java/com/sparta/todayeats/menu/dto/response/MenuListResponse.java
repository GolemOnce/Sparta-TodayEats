package com.sparta.todayeats.menu.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.todayeats.menu.entity.Menu;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class MenuListResponse {

    private List<MenuDetailResponse> content;

    private int page;
    private int size;

    @JsonProperty("total_elements")
    private long totalElements;

    @JsonProperty("total_pages")
    private int totalPages;

    private String sort;

    public static MenuListResponse from(Page<Menu> pageData) {
        return MenuListResponse.builder()
                .content(
                        pageData.getContent()
                                .stream()
                                .map(MenuDetailResponse::from)
                                .toList()
                )
                .page(pageData.getNumber())
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .sort(pageData.getSort().toString())
                .build();
    }
}
