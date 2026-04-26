package com.sparta.todayeats.menu.domain.repository;

import com.sparta.todayeats.menu.domain.entity.MenuEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {

    // 사장님 메뉴 조회 (숨김 메뉴 포함, 삭제 메뉴 제외, 검색 + 페이징)
    @Query("""
        SELECT m FROM MenuEntity m
        WHERE m.store.id = :storeId
        AND m.isDeleted = false
        AND (:keyword IS NULL OR m.name LIKE CONCAT('%', :keyword, '%'))
    """)
    Page<MenuEntity> findOwnerMenusByStoreId(
            @Param("storeId") UUID storeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 고객 메뉴 조회 (숨김 메뉴 제외, 삭제 메뉴 제외, 검색 + 페이징)
    @Query("""
        SELECT m FROM MenuEntity m
        WHERE m.store.id = :storeId
        AND m.isHidden = false
        AND m.isDeleted = false
        AND (:keyword IS NULL OR m.name LIKE CONCAT('%', :keyword, '%'))
    """)
    Page<MenuEntity> findVisibleMenusByStoreId(
            @Param("storeId") UUID storeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 주문 가능한 메뉴 조회 (숨김 메뉴 제외, 삭제 메뉴 제외, 품절 메뉴 제외, 검색 + 페이징)
    @Query("""
        SELECT m FROM MenuEntity m
        WHERE m.store.id = :storeId
        AND m.isHidden = false
        AND m.isDeleted = false
        AND m.soldOut = false
        AND (:keyword IS NULL OR m.name LIKE CONCAT('%', :keyword, '%'))
    """)
    Page<MenuEntity> findOrderableMenusByStoreId(
            @Param("storeId") UUID storeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 카테고리별 조회 (삭제 메뉴 제외)
    @Query("""
        SELECT m FROM MenuEntity m
        WHERE m.category.id = :categoryId
        AND m.isDeleted = false
    """)
    List<MenuEntity> findMenusByCategoryId(@Param("categoryId") UUID categoryId);

    // 가게 + 카테고리 + 노출된 메뉴 (고객용, 검색 + 페이징)
    @Query("""
        SELECT m FROM MenuEntity m
        WHERE m.store.id = :storeId
        AND m.category.id = :categoryId
        AND m.isHidden = false
        AND m.isDeleted = false
        AND (:keyword IS NULL OR m.name LIKE CONCAT('%', :keyword, '%'))
    """)
    Page<MenuEntity> findVisibleMenusByStoreAndCategory(
            @Param("storeId") UUID storeId,
            @Param("categoryId") UUID categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}