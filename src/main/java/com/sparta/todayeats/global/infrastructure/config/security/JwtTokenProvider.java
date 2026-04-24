package com.sparta.todayeats.global.infrastructure.config.security;

import com.sparta.todayeats.global.exception.AuthErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
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

    private static final String TOKEN_TYPE = "tokenType";
    private static final String ROLE = "role";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

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
        return createToken(userId, role, accessTokenValidity, ACCESS);
    }

    // Refresh Token 생성
    public String createRefreshToken(UUID userId) {
        return createToken(userId, null, refreshTokenValidity, REFRESH);
    }

    // Token 생성
    private String createToken(UUID userId, UserRoleEnum role, long validity, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .claim(TOKEN_TYPE, tokenType)
                .signWith(key);

        if (role != null) {
            builder.claim(ROLE, role.getAuthority());
        }

        return builder.compact();
    }

    // Authentication 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if (!ACCESS.equals(claims.get(TOKEN_TYPE, String.class))) {
            throw new BaseException(AuthErrorCode.INVALID_TOKEN);
        }

        String role = Optional.ofNullable(claims.get(ROLE))
                .map(Object::toString)
                .orElse(null);

        List<SimpleGrantedAuthority> authorities =
                role != null ? List.of(new SimpleGrantedAuthority(role)) : List.of();

        return new UsernamePasswordAuthenticationToken(
                UUID.fromString(claims.getSubject()),
                null,
                authorities
        );
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