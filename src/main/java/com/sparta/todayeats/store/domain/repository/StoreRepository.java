package com.sparta.todayeats.store.domain.repository;

import com.sparta.todayeats.store.domain.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<StoreEntity, UUID> {

    @Query("SELECT s FROM StoreEntity s WHERE s.storeId = :storeId AND s.deletedAt IS NULL AND (s.isHidden IS NULL OR s.isHidden = false)")
    Optional<StoreEntity> findActiveById(@Param("storeId") UUID storeId);
}