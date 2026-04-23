package com.sparta.todayeats.area.domain.repository;

import com.sparta.todayeats.area.domain.entity.Area;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AreaRepository extends JpaRepository<Area, UUID> {
    boolean existsByNameIgnoreCase(String name);
    Page<Area> findByNameContainingIgnoreCase(String name, Pageable pageable);
}