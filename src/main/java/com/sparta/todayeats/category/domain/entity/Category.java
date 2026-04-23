package com.sparta.todayeats.category.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Category {

    @Id
    private UUID id;
}