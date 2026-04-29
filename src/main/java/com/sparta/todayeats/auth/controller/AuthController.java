package com.sparta.todayeats.auth.controller;

import com.sparta.todayeats.auth.dto.request.*;
import com.sparta.todayeats.auth.dto.response.*;
import com.sparta.todayeats.auth.service.AuthService;
import com.sparta.todayeats.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/verify-code/send")
    public ResponseEntity<ApiResponse<CodeResponse>> sendSignupCode(@Valid @RequestBody SendCodeRequest request) {
        CodeResponse response = authService.sendSignupCode(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify-code/confirm")
    public ResponseEntity<ApiResponse<CodeResponse>> confirmSignupCode(@Valid @RequestBody ConfirmCodeRequest request) {
        CodeResponse response = authService.confirmSignupCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.reissue(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password/send")
    public ResponseEntity<ApiResponse<CodeResponse>> sendPasswordResetLink(@Valid @RequestBody SendCodeRequest request) {
        CodeResponse response = authService.sendPasswordResetLink(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reset-password")
    public ResponseEntity<ApiResponse<ConfirmCodeResponse>> confirmPasswordResetLink(@RequestParam String code) {
        ConfirmCodeResponse response = authService.confirmPasswordResetLink(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(
            @RequestParam String code, @Valid @RequestBody ResetPasswordRequest request
    ) {
        ResetPasswordResponse response = authService.resetPassword(
                code, request.getNewPassword(), request.getConfirmNewPassword()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}