package com.sparta.todayeats.menu.repository;

import com.sparta.todayeats.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    // is_hidden = false 조건 추가
    @Query("SELECT m FROM Menu m WHERE m.menuId = :menuId AND m.deletedAt IS NULL AND (m.isHidden = false OR m.isHidden IS NULL)")
    Optional<Menu> findActiveById(@Param("menuId") UUID menuId);
}