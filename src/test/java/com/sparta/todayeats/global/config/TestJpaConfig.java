package com.sparta.todayeats.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@TestConfiguration
@EnableJpaAuditing(auditorAwareRef = "loginUserAuditorAware")
public class TestJpaConfig {
    @Bean
    public AuditorAware<UUID> loginUserAuditorAware() {
        return () -> Optional.of(UUID.randomUUID());
    }
}