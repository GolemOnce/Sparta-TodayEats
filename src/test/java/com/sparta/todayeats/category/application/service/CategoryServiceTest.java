package com.sparta.todayeats.category.application.service;

import com.sparta.todayeats.category.domain.entity.Category;
import com.sparta.todayeats.category.domain.repository.CategoryRepository;
import com.sparta.todayeats.category.presentation.dto.*;
import com.sparta.todayeats.global.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    // 카테고리 생성
    @Nested
    @DisplayName("createCategory()")
    class CreateCategory {

        @Test
        @DisplayName("성공 - 카테고리 생성")
        void success() {
            // given
            CategoryCreateRequest request = new CategoryCreateRequest("한식");

            given(categoryRepository.existsByName("한식"))
                    .willReturn(false);

            given(categoryRepository.save(any(Category.class)))
                    .willAnswer(invocation -> {
                        Category c = invocation.getArgument(0);
                        return Category.builder()
                                .id(UUID.randomUUID())
                                .name(c.getName())
                                .build();
                    });

            // when
            CategoryCreateResponse result =
                    categoryService.createCategory(request);

            // then
            assertThat(result.getName()).isEqualTo("한식");
        }

        @Test
        @DisplayName("실패 - 중복 카테고리")
        void fail_duplicate() {
            // given
            CategoryCreateRequest request = new CategoryCreateRequest("한식");

            given(categoryRepository.existsByName("한식"))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() ->
                    categoryService.createCategory(request)
            ).isInstanceOf(BaseException.class);
        }
    }

    // 카테고리 조회
    @Nested
    @DisplayName("getCategories()")
    class GetCategories {

        @Test
        @DisplayName("성공 - 전체 조회")
        void success_all() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            Category category = Category.builder()
                    .id(UUID.randomUUID())
                    .name("한식")
                    .build();

            Page<Category> pageResult = new PageImpl<>(
                    List.of(category),
                    pageable,
                    1
            );

            given(categoryRepository.findAll(pageable))
                    .willReturn(pageResult);

            // when
            PageResponse<CategoryResponse> result =
                    categoryService.getCategories(null, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("한식");
        }

        @Test
        @DisplayName("성공 - 검색 조회")
        void success_search() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            Category category = Category.builder()
                    .id(UUID.randomUUID())
                    .name("한식")
                    .build();

            Page<Category> pageResult = new PageImpl<>(
                    List.of(category),
                    pageable,
                    1
            );

            given(categoryRepository.findByNameContainingIgnoreCase("한", pageable))
                    .willReturn(pageResult);

            // when
            PageResponse<CategoryResponse> result =
                    categoryService.getCategories("한", pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("한식");
        }
    }

    // 카테고리 상세 조회
    @Nested
    @DisplayName("getCategory()")
    class GetCategory {

        @Test
        @DisplayName("성공 - 단건 조회")
        void success() {
            // given
            UUID id = UUID.randomUUID();

            Category category = Category.builder()
                    .id(id)
                    .name("한식")
                    .build();

            given(categoryRepository.findById(id))
                    .willReturn(Optional.of(category));

            // when
            CategoryResponse result = categoryService.getCategory(id);

            // then
            assertThat(result.getName()).isEqualTo("한식");
        }

        @Test
        @DisplayName("실패 - 카테고리 없음")
        void fail_not_found() {
            // given
            UUID id = UUID.randomUUID();

            given(categoryRepository.findById(id))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    categoryService.getCategory(id)
            ).isInstanceOf(BaseException.class);
        }
    }

    // 카테고리 수정
    @Nested
    @DisplayName("updateCategory()")
    class UpdateCategory {

        @Test
        @DisplayName("성공 - 카테고리 수정")
        void success() {
            // given
            UUID id = UUID.randomUUID();

            Category category = Category.builder()
                    .id(id)
                    .name("한식")
                    .build();

            given(categoryRepository.findById(id))
                    .willReturn(Optional.of(category));

            CategoryUpdateRequest request =
                    new CategoryUpdateRequest("중식");

            // when
            CategoryResponse result =
                    categoryService.updateCategory(id, request);

            // then
            assertThat(result.getName()).isEqualTo("중식");
        }

        @Test
        @DisplayName("실패 - 카테고리 없음")
        void fail_not_found() {
            // given
            UUID id = UUID.randomUUID();

            given(categoryRepository.findById(id))
                    .willReturn(Optional.empty());

            CategoryUpdateRequest request =
                    new CategoryUpdateRequest("중식");

            // when & then
            assertThatThrownBy(() ->
                    categoryService.updateCategory(id, request)
            ).isInstanceOf(BaseException.class);
        }
    }

    // 카테고리 삭제
    @Nested
    @DisplayName("deleteCategory()")
    class DeleteCategory {

        @Test
        @DisplayName("성공 - 카테고리 삭제")
        void success() {
            // given
            UUID id = UUID.randomUUID();

            Category category = Category.builder()
                    .id(id)
                    .name("한식")
                    .build();

            given(categoryRepository.findById(id))
                    .willReturn(Optional.of(category));

            // when
            categoryService.deleteCategory(id);

            // then
            assertThat(category.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("실패 - 카테고리 없음")
        void fail_not_found() {
            // given
            UUID id = UUID.randomUUID();

            given(categoryRepository.findById(id))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    categoryService.deleteCategory(id)
            ).isInstanceOf(BaseException.class);
        }
    }
}
