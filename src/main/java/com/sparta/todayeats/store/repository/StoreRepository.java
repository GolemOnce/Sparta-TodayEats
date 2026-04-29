package com.sparta.todayeats.store.repository;

import com.sparta.todayeats.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID>, StoreRepositoryCustom {

    // 이름 중복 확인
    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    // store 조회 시 owner도 한번에 가져오기
    @Query("SELECT s FROM Store s JOIN FETCH s.owner WHERE s.id = :storeId")
    Optional<Store> findByIdWithOwner(@Param("storeId") UUID storeId);

    // 카테고리에 속한 삭제되지 않은 가게 존재 여부 확인
    boolean existsByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    // 운영지역에 속한 삭제되지 않은 가게 존재 여부 확인
    boolean existsByAreaIdAndDeletedAtIsNull(UUID areaId);
}
