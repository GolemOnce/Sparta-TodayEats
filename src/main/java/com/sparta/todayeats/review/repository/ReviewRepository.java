package com.sparta.todayeats.review.repository;

import com.sparta.todayeats.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    @Query(
            value = "SELECT r FROM Review r " +
                    "JOIN FETCH r.user " +
                    "JOIN FETCH r.store " +
                    "JOIN FETCH r.order " +
                    "WHERE r.user.userId = :userId AND r.deletedAt IS NULL",
            countQuery = "SELECT COUNT(r) FROM Review r " +
                    "JOIN r.user " +
                    "WHERE r.user.userId = :userId AND r.deletedAt IS NULL"
    )
    Page<Review> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    // 가게 리뷰 조회
    @Query(
            value = "SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.store JOIN FETCH r.order WHERE r.store.id = :storeId AND r.deletedAt IS NULL",
            countQuery = "SELECT COUNT(r) FROM Review r JOIN r.store WHERE r.store.id = :storeId AND r.deletedAt IS NULL"
    )
    Page<Review> findByStoreId(@Param("storeId") UUID storeId, Pageable pageable);
}
