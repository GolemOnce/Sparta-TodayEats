package com.sparta.todayeats.global.infrastructure.config.security;

import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Duration;
import java.util.*;

@Component
public class JwtTokenProvider {
    private final Key key;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-expiration}") long accessTokenValidity,
            @Value("${jwt.refresh-expiration}") long refreshTokenValidity
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    // Access Token 생성
    public String createAccessToken(UUID userId, UserRoleEnum role) {
        return createToken(userId, role, accessTokenValidity);
    }

    // Refresh Token 생성
    public String createRefreshToken(UUID userId) {
        return createToken(userId, null, refreshTokenValidity);
    }

    // Token 생성
    private String createToken(UUID userId, UserRoleEnum role, long validity) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key);

        if (role != null) {
            builder.claim("role", role.getAuthority());
        }

        return builder.compact();
    }

    // Authentication 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String role = Optional.ofNullable(claims.get("role"))
                .map(Object::toString)
                .orElse(null);

        List<SimpleGrantedAuthority> authorities =
                role != null ? List.of(new SimpleGrantedAuthority(role)) : List.of();

        return new UsernamePasswordAuthenticationToken(getUserId(token), "", authorities);
    }

    // Token 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // userId 추출
    public UUID getUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    // Refresh Token 만료 시간 추출
    public Duration getRefreshTokenValidityDuration() {
        return Duration.ofMillis(refreshTokenValidity);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}