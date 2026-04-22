package com.sparta.todayeats.category.presentation.controller;

import com.sparta.todayeats.category.application.service.CategoryService;
import com.sparta.todayeats.category.presentation.dto.CategoryCreateRequest;
import com.sparta.todayeats.category.presentation.dto.CategoryCreateResponse;
import com.sparta.todayeats.category.presentation.dto.CategoryResponse;
import com.sparta.todayeats.category.presentation.dto.PageResponse;
import com.sparta.todayeats.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 생성
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryCreateResponse>> createCategory(@RequestBody CategoryCreateRequest request) {
        CategoryCreateResponse response = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    // 카테고리 목록 조회 + 검색
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getCategories(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        PageResponse<CategoryResponse> response = categoryService.getCategories(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 카테고리 상세 조회
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable UUID categoryId) {

        CategoryResponse response = categoryService.getCategory(categoryId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}