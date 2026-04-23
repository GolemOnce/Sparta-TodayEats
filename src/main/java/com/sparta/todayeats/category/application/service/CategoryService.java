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

        // 입력값 정규화
        String name = normalizeName(request.getName());

        // 카테고리 이름 중복 여부 검증
        validateDuplicateCategory(name);

        // 카테고리 엔티티 생성
        Category category = Category.builder()
                .name(name)
                .build();

        // DB 저장
        Category saved = categoryRepository.save(category);

        // 응답 DTO로 변환
        return CategoryCreateResponse.builder()
                .categoryId(saved.getId())
                .name(saved.getName())
                .createdAt(saved.getCreatedAt())
                .createdBy(saved.getCreatedBy())
                .build();
    }


    // 카테고리 목록 조회 && 검색
    public PageResponse<CategoryResponse> getCategories(String keyword, Pageable pageable) {

        // keyword가 없으면 전체 조회, 있으면 이름 기준 검색
        Page<Category> result = findCategories(keyword, pageable);

        // 엔티티 → DTO 변환
        List<CategoryResponse> content = result.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        // 페이지 응답 DTO 생성
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

        // ID로 카테고리 조회 후 DTO 변환
        Category category = getCategoryEntity(categoryId);
        return toResponse(category);
    }


    // 카테고리 수정
    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, CategoryUpdateRequest request) {

        // 수정 대상 카테고리 조회
        Category category = getCategoryEntity(categoryId);

        // 정규화
        String name = normalizeName(request.getName());

        // 기존 이름과 다른 경우에만 중복 검증 수행
        if (!category.getName().equals(name)) {
            validateDuplicateCategory(name);
        }

        // 카테고리 이름 수정
        category.updateName(name);

        return toResponse(category);
    }


    // 카테고리 삭제
    @Transactional
    public void deleteCategory(UUID categoryId) {

        Category category = getCategoryEntity(categoryId);

        // 소프트 삭제 처리
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
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BaseException(CategoryErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

    // 카테고리 이름 정규화
    private String normalizeName(String name) {
        // null인 경우 예외 처리
        if (name == null) throw new BaseException(CategoryErrorCode.INVALID_CATEGORY_NAME);

        // 앞뒤 공백 제거
        String normalized = name.trim();

        // 공백일 경우 예외처리
        if (normalized.isBlank()) throw new BaseException(CategoryErrorCode.INVALID_CATEGORY_NAME);

        return normalized;
    }

    // 카테고리 엔티티 조회
    private Category getCategoryEntity(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BaseException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }

    // 카테고리 목록 조회 및 검색 처리
    private Page<Category> findCategories(String keyword, Pageable pageable) {
        // keyword가 없으면 전체 조회
        if (keyword == null || keyword.isBlank()) {
            return categoryRepository.findAll(pageable);
        }

        return categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }
}
