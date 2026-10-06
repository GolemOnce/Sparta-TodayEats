package com.sparta.todayeats.category.controller;

import com.sparta.todayeats.category.dto.request.CategoryCreateRequest;
import com.sparta.todayeats.category.dto.response.CategoryCreateResponse;
import com.sparta.todayeats.category.dto.response.CategoryResponse;
import com.sparta.todayeats.category.dto.request.CategoryUpdateRequest;
import com.sparta.todayeats.category.service.CategoryService;
import com.sparta.todayeats.global.annotation.ApiNoContent;
import com.sparta.todayeats.global.annotation.ApiPageable;
import com.sparta.todayeats.global.annotation.LoginUser;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Category")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 생성")
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    public ResponseEntity<ApiResponse<CategoryCreateResponse>> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryCreateResponse response = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @Operation(summary = "카테고리 목록 조회")
    @ApiPageable
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getCategories(
            @Parameter(description = "카테고리 이름", example = "한식")
            @RequestParam(required = false) String keyword,
            @Parameter(hidden = true)
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        PageResponse<CategoryResponse> response = categoryService.getCategories(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "카테고리 상세 조회")
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
            @Parameter(description = "카테고리 ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
            @PathVariable UUID categoryId
    ) {

        CategoryResponse response = categoryService.getCategory(categoryId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "카테고리 수정")
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "카테고리 ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {

        CategoryResponse response = categoryService.updateCategory(categoryId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "카테고리 삭제")
    @ApiNoContent
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "카테고리 ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
            @PathVariable UUID categoryId,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {

        categoryService.deleteCategory(categoryId,userId);

        return ResponseEntity.noContent().build();
    }
}