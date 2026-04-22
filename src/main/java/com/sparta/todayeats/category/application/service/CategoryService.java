package com.sparta.todayeats.category.application.service;

import com.sparta.todayeats.category.domain.entity.Category;
import com.sparta.todayeats.category.domain.repository.CategoryRepository;
import com.sparta.todayeats.category.presentation.dto.*;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.CategoryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
    public PageResponse<CategoryResponse> getCategories(String keyword, Pageable pageable) {

        Page<Category> result;

        // keyword 유무에 따라 전체 조회 또는 이름 검색 조회
        if (keyword == null || keyword.isBlank()) {
            result = categoryRepository.findAll(pageable);
        } else {
            result = categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }

        // DTO 리스트로 변환
        List<CategoryResponse> content = result.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<CategoryResponse>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .sort(pageable.getSort().toString())
                .build();
    }


    // 카테고리 상세 조회
    public CategoryResponse getCategory(UUID categoryId) {

        Category category = getCategoryEntity(categoryId);
        return toResponse(category);
    }


    // 카테고리 수정
    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, CategoryUpdateRequest request) {

        Category category = getCategoryEntity(categoryId);

        category.updateName(request.getName());

        return toResponse(category);
    }


    // 카테고리 삭제
    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category category = getCategoryEntity(categoryId);

        category.softDelete(null);
    }


    // Category 엔티티 → 목록 응답 DTO 변환
    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .createdBy(category.getCreatedBy())
                .updatedAt(category.getUpdatedAt())
                .updatedBy(category.getUpdatedBy())
                .build();
    }


    // 카테고리 이름 기준 중복 조회 (존재 여부 확인)
    private void validateDuplicateCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BaseException(CategoryErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }


    // 카테고리 조회
    private Category getCategoryEntity(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BaseException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }
}
