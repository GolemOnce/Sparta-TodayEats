package com.sparta.todayeats.area.repository;

import com.sparta.todayeats.area.entity.Area;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AreaRepository extends JpaRepository<Area, UUID> {
    // 운영지역 이름 중복 여부 확인 (대소문자 무시)
    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    // 운영지역 이름 기준 부분 일치(대소문자 무시) 검색 + 페이징 조회
    Page<Area> findByNameContainingIgnoreCase(String name, Pageable pageable);
}