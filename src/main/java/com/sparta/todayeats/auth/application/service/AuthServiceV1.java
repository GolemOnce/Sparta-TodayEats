package com.sparta.todayeats.auth.application.service;

import com.sparta.todayeats.auth.presentation.dto.request.SignupRequest;
import com.sparta.todayeats.auth.presentation.dto.response.*;
import com.sparta.todayeats.global.exception.AuthErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.UserErrorCode;
import com.sparta.todayeats.global.infrastructure.config.security.JwtTokenProvider;
import com.sparta.todayeats.user.domain.entity.User;
import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import com.sparta.todayeats.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceV1 {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMailService authMailService;
    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String SIGNUP_PREFIX = "AUTH_SIGNUP:";
    private static final String VERIFIED_PREFIX = "AUTH_VERIFIED:";
    private static final String RT_PREFIX = "AUTH_RT:";
    private static final String RESET_PASSWORD_PREFIX = "AUTH_RESET_PASSWORD:";

    private static final long CODE_VALID_MINUTES = 5;
    private static final long VERIFIED_VALID_MINUTES = 10;

    public SignupResponse signup(SignupRequest request) {
        // 이메일 인증 여부 조회
        String email = request.getEmail();
        String verifiedKey = VERIFIED_PREFIX + email;
        if (!redisTemplate.hasKey(verifiedKey)) {
            throw new BaseException(AuthErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 비밀번호 일치 확인
        String password = request.getPassword();
        if (!password.equals(request.getConfirmPassword())) {
            throw new BaseException(UserErrorCode.PASSWORD_MISMATCH);
        }

        // 사용자 생성 및 저장
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(request.getNickname())
                .role(request.getRole())
                .build();
        userRepository.save(user);

        // Redis에 이메일 인증 여부 삭제
        redisTemplate.delete(verifiedKey);

        return new SignupResponse(user);
    }

    public SendCodeResponse sendSignupCode(String email) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new BaseException(UserErrorCode.DUPLICATE_EMAIL);
        }

        // 인증번호 생성
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // Redis에 인증번호 저장
        redisTemplate.opsForValue().set(
                SIGNUP_PREFIX + email,
                code,
                Duration.ofMinutes(CODE_VALID_MINUTES)
        );

        // 메일 전송
        authMailService.sendSignupCode(email, code);

        return new SendCodeResponse(email, LocalDateTime.now().plusMinutes(CODE_VALID_MINUTES));
    }

    public ConfirmCodeResponse confirmSignupCode(String email, String code) {
        // 인증번호 조회
        String signupKey = SIGNUP_PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(signupKey);
        if (savedCode == null || !savedCode.equals(code)) {
            throw new BaseException(AuthErrorCode.INVALID_VERIFICATION_CODE);
        }

        // Redis에 인증번호 삭제
        redisTemplate.delete(signupKey);

        // Redis에 이메일 인증 여부 저장
        redisTemplate.opsForValue().set(
                VERIFIED_PREFIX + email,
                "true",
                Duration.ofMinutes(VERIFIED_VALID_MINUTES)
        );

        return new ConfirmCodeResponse(email);
    }

    public LoginResponse login(String email, String password) {
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BaseException(UserErrorCode.PASSWORD_MISMATCH);
        }

        // Token 생성
        UUID userId = user.getUserId();
        UserRoleEnum role = user.getRole();
        String accessToken = jwtTokenProvider.createAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        // Redis에 Refresh Token 저장
        redisTemplate.opsForValue().set(
                RT_PREFIX + userId,
                refreshToken,
                jwtTokenProvider.getRefreshTokenValidityDuration()
        );

        return LoginResponse.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .role(role.name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenResponse reissue(String refreshToken) {
        // 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BaseException(AuthErrorCode.INVALID_TOKEN);
        }

        // userId 추출
        UUID userId = jwtTokenProvider.getUserId(refreshToken);

        // Refresh Token 조회
        String rtKey = RT_PREFIX + userId;
        String savedRefreshToken = redisTemplate.opsForValue().get(rtKey);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new BaseException(AuthErrorCode.INVALID_TOKEN);
        }

        // Access Token & Refresh Token 재발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        // Redis에 Refresh Token 저장
        redisTemplate.opsForValue().set(
                rtKey,
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenValidityDuration()
        );

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public void logout(Authentication authentication) {
        // Redis에서 Refresh Token 삭제
        redisTemplate.delete(RT_PREFIX + authentication.getPrincipal());
    }

    public SendCodeResponse sendPasswordResetLink(String email) {
        // 이메일 존재 여부 확인
        if (!userRepository.existsByEmail(email)) {
            throw new BaseException(UserErrorCode.USER_NOT_FOUND);
        }

        // 인증번호 생성
        String code = UUID.randomUUID().toString();

        // Redis에 인증번호 저장
        redisTemplate.opsForValue().set(
                RESET_PASSWORD_PREFIX + code,
                email,
                Duration.ofMinutes(CODE_VALID_MINUTES)
        );

        // 메일 전송
        authMailService.sendResetPasswordLink(email, code);

        return new SendCodeResponse(email, LocalDateTime.now().plusMinutes(CODE_VALID_MINUTES));
    }

    @Transactional(readOnly = true)
    public ConfirmCodeResponse confirmPasswordResetLink(String code) {
        // 인증번호 조회
        return new ConfirmCodeResponse(getEmailByResetCode(code));
    }

    public PasswordResetResponse passwordReset(String code, String newPassword, String confirmPassword) {
        // 인증번호 조회
        String email = getEmailByResetCode(code);

        // 비밀번호 일치 확인
        if (!newPassword.equals(confirmPassword)) {
            throw new BaseException(UserErrorCode.PASSWORD_MISMATCH);
        }

        // 사용자 조회 및 비밀번호 변경
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        user.updatePassword(passwordEncoder.encode(newPassword));

        // Redis에 인증번호 삭제
        redisTemplate.delete(RESET_PASSWORD_PREFIX + code);

        return new PasswordResetResponse(user.getEmail(), user.getUpdatedAt());
    }

    private String getEmailByResetCode(String code) {
        String email = redisTemplate.opsForValue().get(RESET_PASSWORD_PREFIX + code);
        if (email == null) {
            throw new BaseException(AuthErrorCode.INVALID_VERIFICATION_CODE);
        }

        return email;
    }
}