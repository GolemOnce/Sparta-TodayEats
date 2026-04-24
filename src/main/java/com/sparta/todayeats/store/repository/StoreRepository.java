package com.sparta.todayeats.store.repository;

import com.sparta.todayeats.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    // 이름 중복 확인
    boolean existsByNameIgnoreCase(String name);
}
