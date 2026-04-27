package com.sparta.todayeats.global.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    // 현재 로그인한 사용자의 UUID를 Auditing에 제공
    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)        // 인증된 사용자인지 확인
                .filter(auth -> !auth.getName().equals("anonymousUser"))  // 비로그인 제외
                .map(Authentication::getName)
                .map(UUID::fromString);                         // String → UUID 변환
    }
}