package com.sparta.todayeats.user.repository;

import com.sparta.todayeats.user.entity.User;
import com.sparta.todayeats.user.entity.UserRoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(UUID userId);

    @Query("""
        SELECT u
        FROM User u
        WHERE (
            :keyword IS NULL
            OR u.email LIKE CONCAT('%', :keyword, '%')
            OR u.nickname LIKE CONCAT('%', :keyword, '%')
        )
        AND (:role IS NULL OR u.role = :role)
    """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") UserRoleEnum role,
            Pageable pageable
    );
}