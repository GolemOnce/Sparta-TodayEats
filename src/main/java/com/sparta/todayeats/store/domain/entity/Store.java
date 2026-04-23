package com.sparta.todayeats.store.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Store {

    @Id
    private UUID id;
}