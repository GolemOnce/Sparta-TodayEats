package com.sparta.todayeats.global.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @CreatedBy
    @Column(updatable = false, length = 36)
    private String createdBy;

    @LastModifiedBy
    @Column(length = 36)
    private String updatedBy;

    @Column(length = 36)
    private String deletedBy;

    public void softDelete(String userId) {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
            this.deletedBy = userId;
        }
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}