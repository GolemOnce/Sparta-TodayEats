package com.sparta.todayeats.address.repository;

import com.sparta.todayeats.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    @Query("SELECT a FROM Address a WHERE a.addressId = :addressId AND a.deletedAt IS NULL")
    Optional<Address> findActiveById(@Param("addressId") UUID addressId);
}