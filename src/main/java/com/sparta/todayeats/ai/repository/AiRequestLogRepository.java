package com.sparta.todayeats.ai.repository;

import com.sparta.todayeats.ai.entity.AiRequestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiRequestLogRepository extends JpaRepository<AiRequestLogEntity, UUID> {
}
