package com.sparta.todayeats.category.application.service;

import com.sparta.todayeats.category.domain.entity.Category;
import com.sparta.todayeats.category.domain.repository.CategoryRepository;
import com.sparta.todayeats.category.presentation.dto.CategoryCreateRequest;
import com.sparta.todayeats.category.presentation.dto.CategoryResponse;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.CategoryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 카테고리 생성
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new BaseException(CategoryErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        Category category = Category.builder()
                .name(request.getName())
                .build();

        Category saved = categoryRepository.save(category);

        return CategoryResponse.builder()
                .categoryId(saved.getId())
                .name(saved.getName())
                .createdAt(saved.getCreatedAt())
                .createdBy(saved.getCreatedBy())
                .build();
    }

    // 동일한 이름의 카테고리가 존재하는지 확인
    private void validateDuplicateCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BaseException(CategoryErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }
}
