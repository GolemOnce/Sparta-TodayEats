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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceV1Test {
    @InjectMocks
    private AuthServiceV1 authServiceV1;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthMailService authMailService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private static final String SIGNUP_PREFIX = "AUTH_SIGNUP:";
    private static final String VERIFIED_PREFIX = "AUTH_VERIFIED:";
    private static final String RT_PREFIX = "AUTH_RT:";
    private static final String RESET_PASSWORD_PREFIX = "AUTH_RESET_PASSWORD:";

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL = "test@test.com";
    private static final String PASSWORD = "decoded";
    private static final String ENCODED_PASSWORD = "encoded";
    private static final String NICKNAME = "nick";
    private static final String CODE = "123456";

    private static final String ACCESS_TOKEN = "Bearer access-token";
    private static final String REFRESH_TOKEN = "Bearer refresh-token";
    private static final String PURE_REFRESH_TOKEN = "refresh-token";

    private static final String RT_KEY = RT_PREFIX + USER_ID;

    @Nested
    @DisplayName("signup()")
    class Signup {
        @Test
        void 회원가입_성공() {
            // given
            SignupRequest request = signupRequest(PASSWORD);

            given(redisTemplate.hasKey(VERIFIED_PREFIX + EMAIL)).willReturn(true);
            given(userRepository.save(any())).willAnswer(i -> i.getArgument(0));
            given(passwordEncoder.encode(any())).willReturn(ENCODED_PASSWORD);

            // when
            SignupResponse response = authServiceV1.signup(request);

            // then
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            verify(userRepository).save(any());
        }

        @Test
        void 회원가입_실패_이메일_미인증() {
            // given
            SignupRequest request = signupRequest(PASSWORD);

            given(redisTemplate.hasKey(VERIFIED_PREFIX + EMAIL)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authServiceV1.signup(request))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(AuthErrorCode.EMAIL_NOT_VERIFIED.getMessage());
        }

        @Test
        void 회원가입_실패_비밀번호_불일치() {
            // given
            SignupRequest request = signupRequest("wrong");

            given(redisTemplate.hasKey(VERIFIED_PREFIX + EMAIL)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authServiceV1.signup(request))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.PASSWORD_MISMATCH.getMessage());
        }

        private SignupRequest signupRequest(String confirmPassword) {
            return SignupRequest.builder()
                    .email(EMAIL)
                    .password(PASSWORD)
                    .confirmPassword(confirmPassword)
                    .nickname(NICKNAME)
                    .role(UserRoleEnum.CUSTOMER)
                    .build();
        }
    }

    @Nested
    @DisplayName("sendSignupCode()")
    class SendSignupCode {
        @Test
        void 인증번호_전송_성공_신규() {
            // given
            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            SendCodeResponse response = authServiceV1.sendSignupCode(EMAIL);

            // then
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            verify(valueOperations).set(eq(SIGNUP_PREFIX + EMAIL), anyString(), any(Duration.class));
            verify(authMailService).sendSignupCode(eq(EMAIL), anyString());
        }

        @Test
        void 인증번호_전송_성공_기존() {
            // given
            User user = User.builder().email(EMAIL).build();
            user.softDelete(USER_ID);

            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            SendCodeResponse response = authServiceV1.sendSignupCode(EMAIL);

            // then
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            verify(valueOperations).set(eq(SIGNUP_PREFIX + EMAIL), anyString(), any(Duration.class));
            verify(authMailService).sendSignupCode(eq(EMAIL), anyString());
        }

        @Test
        void 인증번호_전송_실패() {
            // given
            User activeUser = User.builder().email(EMAIL).build();

            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(activeUser));

            // when & then
            assertThatThrownBy(() -> authServiceV1.sendSignupCode(EMAIL))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.DUPLICATE_EMAIL.getMessage());
        }
    }

    @Nested
    @DisplayName("confirmSignupCode()")
    class ConfirmSignupCode {
        @Test
        void 인증번호_검증_성공() {
            // given
            String signupKey = SIGNUP_PREFIX + EMAIL;
            String verifiedKey = VERIFIED_PREFIX + EMAIL;

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(signupKey)).willReturn(CODE);

            // when
            ConfirmCodeResponse response = authServiceV1.confirmSignupCode(EMAIL, CODE);

            // then
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            verify(redisTemplate).delete(signupKey);
            verify(valueOperations).set(eq(verifiedKey), eq("true"), any());
        }

        @Test
        void 인증번호_검증_실패_인증번호_없음() {
            // given
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(SIGNUP_PREFIX + EMAIL)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authServiceV1.confirmSignupCode(EMAIL, CODE))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(AuthErrorCode.INVALID_VERIFICATION_CODE.getMessage());
        }

        @Test
        void 인증번호_검증_실패_인증번호_불일치() {
            // given
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(SIGNUP_PREFIX + EMAIL)).willReturn("wrong");

            // when & then
            assertThatThrownBy(() -> authServiceV1.confirmSignupCode(EMAIL, CODE))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(AuthErrorCode.INVALID_VERIFICATION_CODE.getMessage());
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {
        @Test
        void 로그인_성공() {
            // given
            User user = User.builder()
                    .email(EMAIL)
                    .password(ENCODED_PASSWORD)
                    .nickname(NICKNAME)
                    .role(UserRoleEnum.CUSTOMER)
                    .build();

            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);
            given(jwtTokenProvider.createAccessToken(any(), any())).willReturn(ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(any())).willReturn(REFRESH_TOKEN);
            given(jwtTokenProvider.substringToken(REFRESH_TOKEN)).willReturn(PURE_REFRESH_TOKEN);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(jwtTokenProvider.getRefreshTokenValidityDuration()).willReturn(Duration.ofDays(7));

            // when
            LoginResponse response = authServiceV1.login(EMAIL, PASSWORD);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            verify(redisTemplate.opsForValue()).set(startsWith(RT_PREFIX), eq(PURE_REFRESH_TOKEN), any());
        }

        @Test
        void 로그인_실패_사용자_없음() {
            // given
            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authServiceV1.login(EMAIL, PASSWORD))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        void 로그인_실패_삭제된_사용자() {
            // given
            User user = User.builder().email(EMAIL).build();
            user.softDelete(USER_ID);

            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> authServiceV1.login(EMAIL, PASSWORD))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        void 로그인_실패_비밀번호_불일치() {
            // given
            User user = User.builder().email(EMAIL).password(ENCODED_PASSWORD).build();

            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authServiceV1.login(EMAIL, PASSWORD))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.PASSWORD_MISMATCH.getMessage());
        }
    }

    @Nested
    @DisplayName("reissue()")
    class Reissue {
        @Test
        void 토큰_재발급_성공() {
            // given
            given(jwtTokenProvider.validateToken(PURE_REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.substringToken(REFRESH_TOKEN)).willReturn(PURE_REFRESH_TOKEN);
            given(jwtTokenProvider.getUserId(PURE_REFRESH_TOKEN)).willReturn(USER_ID);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(RT_KEY)).willReturn(PURE_REFRESH_TOKEN);

            User user = User.builder().email(EMAIL).role(UserRoleEnum.CUSTOMER).build();

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(jwtTokenProvider.createAccessToken(any(), any())).willReturn(ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(any())).willReturn(REFRESH_TOKEN);
            given(jwtTokenProvider.substringToken(REFRESH_TOKEN)).willReturn(PURE_REFRESH_TOKEN);
            given(jwtTokenProvider.getRefreshTokenValidityDuration()).willReturn(Duration.ofDays(7));

            // when
            TokenResponse response = authServiceV1.reissue(REFRESH_TOKEN);

            // then
            assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
            verify(valueOperations).set(eq(RT_KEY), eq(PURE_REFRESH_TOKEN), any());
        }

        @Test
        void 토큰_재발급_실패_토큰_미유효() {
            // given
            given(jwtTokenProvider.substringToken(REFRESH_TOKEN)).willReturn(PURE_REFRESH_TOKEN);
            given(jwtTokenProvider.validateToken(PURE_REFRESH_TOKEN)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authServiceV1.reissue(REFRESH_TOKEN))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(AuthErrorCode.INVALID_TOKEN.getMessage());
        }

        @Test
        void 토큰_재발급_실패_토큰_불일치() {
            // given
            given(jwtTokenProvider.validateToken(PURE_REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.substringToken(REFRESH_TOKEN)).willReturn(PURE_REFRESH_TOKEN);
            given(jwtTokenProvider.getUserId(PURE_REFRESH_TOKEN)).willReturn(USER_ID);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(RT_KEY)).willReturn("other-token");

            // when & then
            assertThatThrownBy(() -> authServiceV1.reissue(REFRESH_TOKEN))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(AuthErrorCode.INVALID_TOKEN.getMessage());
        }

        @Test
        void 토큰_재발급_실패_사용자_없음() {
            // given
            given(jwtTokenProvider.validateToken(PURE_REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.substringToken(REFRESH_TOKEN)).willReturn(PURE_REFRESH_TOKEN);
            given(jwtTokenProvider.getUserId(PURE_REFRESH_TOKEN)).willReturn(USER_ID);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(RT_KEY)).willReturn(PURE_REFRESH_TOKEN);
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authServiceV1.reissue(REFRESH_TOKEN))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
        }
    }

    @Test
    @DisplayName("logout()")
    void 로그아웃_성공() {
        // given
        Authentication authentication = mock(Authentication.class);
        given(authentication.getName()).willReturn(USER_ID.toString());

        // when
        authServiceV1.logout(authentication.getName());

        // then
        verify(redisTemplate).delete(RT_KEY);
    }

    @Test
    @DisplayName("sendResetPasswordLink()")
    void 비밀번호_재설정_링크_전송_성공() {
        // given
        User user = User.builder().email(EMAIL).build();

        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        SendCodeResponse response = authServiceV1.sendPasswordResetLink(EMAIL);

        // then
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        verify(valueOperations).set(startsWith(RESET_PASSWORD_PREFIX), eq(EMAIL), any());
        verify(authMailService).sendPasswordResetLink(eq(EMAIL), anyString());
    }

    @Nested
    @DisplayName("resetPassword()")
    class ResetPassword {
        private final String NEW_PASSWORD = "decoded-new";

        @Test
        void 비밀번호_재설정_성공() {
            // given
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(resetKey())).willReturn(EMAIL);

            User user = User.builder().email(EMAIL).password("encoded-old").build();

            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
            given(passwordEncoder.encode(NEW_PASSWORD)).willReturn("encoded-new");

            // when
            ResetPasswordResponse response = authServiceV1.resetPassword(CODE, NEW_PASSWORD, NEW_PASSWORD);

            // then
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            verify(redisTemplate).delete(resetKey());
            verify(passwordEncoder).encode(NEW_PASSWORD);
        }

        @Test
        void 비밀번호_재설정_실패_인증번호_없음() {
            // given
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(resetKey())).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authServiceV1.resetPassword(CODE, NEW_PASSWORD, NEW_PASSWORD))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(AuthErrorCode.INVALID_VERIFICATION_CODE.getMessage());
        }

        @Test
        void 비밀번호_재설정_실패_비밀번호_불일치() {
            // given
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(resetKey())).willReturn(EMAIL);

            // when & then
            assertThatThrownBy(() -> authServiceV1.resetPassword(CODE, "a", "b"))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.PASSWORD_MISMATCH.getMessage());
        }

        @Test
        void 비밀번호_재설정_실패_사용자_없음() {
            // given
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(resetKey())).willReturn(EMAIL);
            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authServiceV1.resetPassword(CODE, NEW_PASSWORD, NEW_PASSWORD))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        void 비밀번호_재설정_실패_삭제된_사용자() {
            // given
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(resetKey())).willReturn(EMAIL);

            User user = User.builder().email(EMAIL).build();
            user.softDelete(USER_ID);

            given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> authServiceV1.resetPassword(CODE, NEW_PASSWORD, NEW_PASSWORD))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
        }

        private String resetKey() {
            return RESET_PASSWORD_PREFIX + CODE;
        }
    }
}