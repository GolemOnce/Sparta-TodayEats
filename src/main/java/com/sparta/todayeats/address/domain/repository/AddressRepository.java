package com.sparta.todayeats.address.domain.repository;

import com.sparta.todayeats.address.domain.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {

    @Query("SELECT a FROM AddressEntity a WHERE a.addressId = :addressId AND a.deletedAt IS NULL")
    Optional<AddressEntity> findActiveById(@Param("addressId") UUID addressId);
}