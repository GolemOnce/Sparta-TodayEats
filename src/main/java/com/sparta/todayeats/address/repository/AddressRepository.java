package com.sparta.todayeats.address.repository;

import com.sparta.todayeats.address.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    @Query(
            value = "SELECT a FROM Address a " +
                    "JOIN FETCH a.user " +
                    "WHERE a.user.userId = :userId AND a.deletedAt IS NULL",
            countQuery = "SELECT COUNT(a) FROM Address a " +
                    "JOIN a.user " +
                    "WHERE a.user.userId = :userId AND a.deletedAt IS NULL"
    )
    Page<Address> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    Optional<Address> findByUserUserIdAndIsDefaultTrue(UUID userId);

    @Query("SELECT a FROM Address a WHERE a.id = :addressId AND a.deletedAt IS NULL")
    Optional<Address> findActiveById(@Param("addressId") UUID addressId);
}
