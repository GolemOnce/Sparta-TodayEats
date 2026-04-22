package com.sparta.todayeats.category.application.service;

import com.sparta.todayeats.category.domain.entity.Category;
import com.sparta.todayeats.category.domain.repository.CategoryRepository;
import com.sparta.todayeats.category.presentation.dto.CategoryCreateRequest;
import com.sparta.todayeats.category.presentation.dto.CategoryCreateResponse;
import com.sparta.todayeats.category.presentation.dto.CategoryListResponse;
import com.sparta.todayeats.category.presentation.dto.PageResponse;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.CategoryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 카테고리 생성
    @Transactional
    public CategoryCreateResponse createCategory(CategoryCreateRequest request) {

        validateDuplicateCategory(request.getName());

        Category category = Category.builder()
                .name(request.getName())
                .build();

        Category saved = categoryRepository.save(category);

        return CategoryCreateResponse.builder()
                .categoryId(saved.getId())
                .name(saved.getName())
                .createdAt(saved.getCreatedAt())
                .createdBy(saved.getCreatedBy())
                .build();
    }

    // 카테고리 목록 조회 && 검색
    public PageResponse<CategoryListResponse> getCategories(String keyword, Pageable pageable) {

        Page<Category> result;

        // keyword 유무에 따라 전체 조회 또는 이름 검색 조회
        if (keyword == null || keyword.isBlank()) {
            result = categoryRepository.findAll(pageable);
        } else {
            result = categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }

        // DTO 리스트로 변환
        List<CategoryListResponse> content = result.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<CategoryListResponse>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .sort(pageable.getSort().toString())
                .build();
    }

    // Category 엔티티 → 목록 응답 DTO 변환
    private CategoryListResponse toResponse(Category category) {
        return CategoryListResponse.builder()
                .categoryId(category.getId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .createdBy(category.getCreatedBy())
                .updatedAt(category.getUpdatedAt())
                .updatedBy(category.getUpdatedBy())
                .build();
    }

    // 동일한 이름의 카테고리가 존재하는지 확인
    private void validateDuplicateCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BaseException(CategoryErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }
}
