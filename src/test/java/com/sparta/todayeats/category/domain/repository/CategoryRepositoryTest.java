package com.sparta.todayeats.category.domain.repository;

import com.sparta.todayeats.category.domain.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@EnableJpaAuditing
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("existsByName - 이름 존재 여부 확인")
    void existsByName() {
        // given
        categoryRepository.save(Category.builder().name("한식").build());

        // when & then
        assertThat(categoryRepository.existsByName("한식")).isTrue();
        assertThat(categoryRepository.existsByName("중식")).isFalse();
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase - 이름 부분 일치 검색")
    void findByNameContainingIgnoreCase() {
        // given
        categoryRepository.save(Category.builder().name("한식").build());
        categoryRepository.save(Category.builder().name("한우").build());
        categoryRepository.save(Category.builder().name("중식").build());
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Category> result = categoryRepository.findByNameContainingIgnoreCase("한", pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("name").containsExactlyInAnyOrder("한식", "한우");
    }
}