package com.sparta.todayeats.category.domain.repository;

import com.sparta.todayeats.category.domain.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    // 카테고리 이름 중복 여부 확인
    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    // 카테고리 이름 기준 부분 일치(대소문자 무시) 검색 + 페이징 조회
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
