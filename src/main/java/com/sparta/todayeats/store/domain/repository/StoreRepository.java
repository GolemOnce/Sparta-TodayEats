package com.sparta.todayeats.store.domain.repository;

import com.sparta.todayeats.store.domain.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<StoreEntity, UUID> {
}
