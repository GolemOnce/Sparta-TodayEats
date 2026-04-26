package com.sparta.todayeats.store.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
public class StoreEntity {

    @Id
    private UUID id;
}
