package com.sparta.todayeats.category.application.service;

import com.sparta.todayeats.category.domain.entity.Category;
import com.sparta.todayeats.category.domain.repository.CategoryRepository;
import com.sparta.todayeats.category.presentation.dto.*;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.response.PageResponse;
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
    @DisplayName("카테고리 생성")
    class CreateCategory {

        @Test
        void 카테고리_생성_성공() {
            // given
            CategoryCreateRequest request = new CategoryCreateRequest("한식");

            given(categoryRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("한식"))
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
        void 카테고리_이름이_중복이면_예외발생() {
            // given
            CategoryCreateRequest request = new CategoryCreateRequest("한식");

            given(categoryRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("한식"))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() ->
                    categoryService.createCategory(request)
            ).isInstanceOf(BaseException.class);
        }

        @Test
        void 카테고리_이름이_null이면_예외() {
            CategoryCreateRequest request = new CategoryCreateRequest(null);

            assertThatThrownBy(() ->
                    categoryService.createCategory(request)
            ).isInstanceOf(BaseException.class);
        }

        @Test
        void 카테고리_이름이_공백이면_예외() {
            CategoryCreateRequest request = new CategoryCreateRequest("   ");

            assertThatThrownBy(() ->
                    categoryService.createCategory(request)
            ).isInstanceOf(BaseException.class);
        }

    }

    // 카테고리 목록 조회
    @Nested
    @DisplayName("카테고리 목록 조회")
    class GetCategories {

        @Test
        void 카테고리_전체조회_성공() {
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
        void 카테고리_이름으로_검색조회_성공() {
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

    // 카테고리 단건 조회
    @Nested
    @DisplayName("카테고리 단건 조회")
    class GetCategory {

        @Test
        void 카테고리_단건조회_성공() {
            // given
            UUID categoryId = UUID.randomUUID();

            Category category = Category.builder()
                    .id(categoryId)
                    .name("한식")
                    .build();

            given(categoryRepository.findById(categoryId))
                    .willReturn(Optional.of(category));

            // when
            CategoryResponse result = categoryService.getCategory(categoryId);

            // then
            assertThat(result.getName()).isEqualTo("한식");
        }

        @Test
        void 카테고리가_존재하지_않으면_예외발생() {
            // given
            UUID categoryId = UUID.randomUUID();

            given(categoryRepository.findById(categoryId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    categoryService.getCategory(categoryId)
            ).isInstanceOf(BaseException.class);
        }
    }

    // 카테고리 수정
    @Nested
    @DisplayName("카테고리 수정")
    class UpdateCategory {

        @Test
        void 카테고리_수정_성공() {
            // given
            UUID categoryId = UUID.randomUUID();

            Category category = Category.builder()
                    .id(categoryId)
                    .name("한식")
                    .build();

            given(categoryRepository.findById(categoryId))
                    .willReturn(Optional.of(category));

            CategoryUpdateRequest request =
                    new CategoryUpdateRequest("중식");

            // when
            CategoryResponse result =
                    categoryService.updateCategory(categoryId, request);

            // then
            assertThat(result.getName()).isEqualTo("중식");
        }

        @Test
        void 카테고리_수정시_존재하지_않으면_예외발생() {
            // given
            UUID categoryId = UUID.randomUUID();

            given(categoryRepository.findById(categoryId))
                    .willReturn(Optional.empty());

            CategoryUpdateRequest request =
                    new CategoryUpdateRequest("중식");

            // when & then
            assertThatThrownBy(() ->
                    categoryService.updateCategory(categoryId, request)
            ).isInstanceOf(BaseException.class);
        }

        @Test
        void 카테고리_수정시_이름이_중복이면_예외() {
            UUID id = UUID.randomUUID();

            Category category = Category.builder()
                    .id(id)
                    .name("한식")
                    .build();

            given(categoryRepository.findById(id))
                    .willReturn(Optional.of(category));

            given(categoryRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("중식"))
                    .willReturn(true);

            CategoryUpdateRequest request = new CategoryUpdateRequest("중식");

            assertThatThrownBy(() ->
                    categoryService.updateCategory(id, request)
            ).isInstanceOf(BaseException.class);
        }
    }

    // 카테고리 삭제
    @Nested
    @DisplayName("카테고리 삭제")
    class DeleteCategory {

        @Test
        void 카테고리_삭제_성공() {
            // given
            UUID categoryId = UUID.randomUUID();

            Category category = Category.builder()
                    .id(categoryId)
                    .name("한식")
                    .build();

            given(categoryRepository.findById(categoryId))
                    .willReturn(Optional.of(category));

            // when
            UUID userId = UUID.randomUUID();
            categoryService.deleteCategory(categoryId, userId);

            // then
            assertThat(category.getDeletedAt()).isNotNull();
        }

        @Test
        void 카테고리_삭제시_존재하지_않으면_예외발생() {
            // given
            UUID categoryId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(categoryRepository.findById(categoryId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    categoryService.deleteCategory(categoryId, userId)
            ).isInstanceOf(BaseException.class);
        }
    }
}
