package com.sparta.todayeats.menu.repository;

import com.sparta.todayeats.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    // 사장님 메뉴 조회 (숨김 메뉴 포함, 삭제 메뉴 제외, 검색 + 페이징)
    @Query("""
        SELECT m FROM Menu m
        WHERE m.store.id = :storeId
        AND m.deletedAt IS NULL
        AND (:keyword IS NULL OR m.name LIKE CONCAT('%', :keyword, '%'))
    """)
    Page<Menu> findOwnerMenusByStoreId(
            @Param("storeId") UUID storeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 고객 메뉴 조회 (숨김 메뉴 제외, 삭제 메뉴 제외, 검색 + 페이징)
    @Query("""
        SELECT m FROM Menu m
        WHERE m.store.id = :storeId
        AND m.isHidden = false
        AND m.deletedAt IS NULL
        AND (:keyword IS NULL OR m.name LIKE CONCAT('%', :keyword, '%'))
    """)
    Page<Menu> findVisibleMenusByStoreId(
            @Param("storeId") UUID storeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 주문 가능한 메뉴 조회 (숨김 메뉴 제외, 삭제 메뉴 제외, 품절 메뉴 제외, 검색 + 페이징)
    @Query("""
        SELECT m FROM Menu m
        WHERE m.store.id = :storeId
        AND m.isHidden = false
        AND m.deletedAt IS NULL
        AND m.soldOut = false
        AND (:keyword IS NULL OR m.name LIKE CONCAT('%', :keyword, '%'))
    """)
    Page<Menu> findOrderableMenusByStoreId(
            @Param("storeId") UUID storeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 카테고리별 조회 (삭제 메뉴 제외)
    @Query("""
        SELECT m FROM Menu m
        WHERE m.category.id = :categoryId
        AND m.deletedAt IS NULL
    """)
    List<Menu> findMenusByCategoryId(@Param("categoryId") UUID categoryId);

    // 가게 + 카테고리 + 노출된 메뉴 (고객용, 검색 + 페이징)
    @Query("""
        SELECT m FROM Menu m
        WHERE m.store.id = :storeId
        AND m.category.id = :categoryId
        AND m.isHidden = false
        AND m.deletedAt IS NULL
        AND (:keyword IS NULL OR m.name LIKE CONCAT('%', :keyword, '%'))
    """)
    Page<Menu> findVisibleMenusByStoreAndCategory(
            @Param("storeId") UUID storeId,
            @Param("categoryId") UUID categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 주문 서비스용 - 삭제되지 않은 메뉴 단건 조회
    @Query("""
        SELECT m FROM Menu m
        WHERE m.id = :menuId
        AND m.deletedAt IS NULL
    """)
    Optional<Menu> findActiveById(@Param("menuId") UUID menuId);
}