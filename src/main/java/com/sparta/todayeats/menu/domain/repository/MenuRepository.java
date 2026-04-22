package com.sparta.todayeats.menu.domain.repository;

import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {

    @Query("SELECT m FROM MenuEntity m WHERE m.menuId = :menuId AND m.deletedAt IS NULL")
    Optional<MenuEntity> findActiveById(@Param("menuId") UUID menuId);
}