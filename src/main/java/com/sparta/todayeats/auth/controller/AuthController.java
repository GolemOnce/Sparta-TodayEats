package com.sparta.todayeats.auth.controller;

import com.sparta.todayeats.auth.dto.request.*;
import com.sparta.todayeats.auth.dto.response.*;
import com.sparta.todayeats.auth.service.AuthService;
import com.sparta.todayeats.global.annotation.ApiNoContent;
import com.sparta.todayeats.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "이메일 인증번호 전송")
    @PostMapping("/verify-code/send")
    public ResponseEntity<ApiResponse<CodeResponse>> sendSignupCode(@Valid @RequestBody SendCodeRequest request) {
        CodeResponse response = authService.sendSignupCode(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "이메일 인증번호 확인")
    @PostMapping("/verify-code/confirm")
    public ResponseEntity<ApiResponse<CodeResponse>> confirmSignupCode(@Valid @RequestBody ConfirmCodeRequest request) {
        CodeResponse response = authService.confirmSignupCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.reissue(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃")
    @ApiNoContent
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "비밀번호 재설정 링크 전송")
    @PostMapping("/reset-password/send")
    public ResponseEntity<ApiResponse<CodeResponse>> sendPasswordResetLink(@Valid @RequestBody SendCodeRequest request) {
        CodeResponse response = authService.sendPasswordResetLink(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "비밀번호 재설정 링크 확인")
    @GetMapping("/reset-password")
    public ResponseEntity<ApiResponse<ConfirmCodeResponse>> confirmPasswordResetLink(
            @Parameter(description = "인증번호", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam String code
    ) {
        ConfirmCodeResponse response = authService.confirmPasswordResetLink(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "비밀번호 재설정")
    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(
            @Parameter(description = "인증번호", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam String code,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        ResetPasswordResponse response = authService.resetPassword(
                code, request.getNewPassword(), request.getConfirmNewPassword()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}