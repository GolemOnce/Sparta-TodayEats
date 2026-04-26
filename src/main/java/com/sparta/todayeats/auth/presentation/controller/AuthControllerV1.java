package com.sparta.todayeats.auth.presentation.controller;

import com.sparta.todayeats.auth.application.service.AuthServiceV1;
import com.sparta.todayeats.auth.presentation.dto.request.*;
import com.sparta.todayeats.auth.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllerV1 {
    private final AuthServiceV1 authServiceV1;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authServiceV1.signup(request));
    }

    @PostMapping("/verify-code/send")
    public ResponseEntity<SendCodeResponse> sendSignupCode(@Valid @RequestBody SendCodeRequest request) {
        return ResponseEntity.ok(authServiceV1.sendSignupCode(request.getEmail()));
    }

    @PostMapping("/verify-code/confirm")
    public ResponseEntity<ConfirmCodeResponse> confirmSignupCode(@Valid @RequestBody ConfirmCodeRequest request) {
        return ResponseEntity.ok(authServiceV1.confirmSignupCode(request.getEmail(), request.getCode()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authServiceV1.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authServiceV1.reissue(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authServiceV1.logout(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password/send")
    public ResponseEntity<SendCodeResponse> sendResetPasswordLink(@Valid @RequestBody SendCodeRequest request) {
        return ResponseEntity.ok(authServiceV1.sendResetPasswordLink(request.getEmail()));
    }

    @GetMapping("/reset-password")
    public ResponseEntity<ConfirmCodeResponse> confirmResetPasswordLink(@RequestParam String code) {
        return ResponseEntity.ok(authServiceV1.confirmResetPasswordLink(code));
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @RequestParam String code, @Valid @RequestBody ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(authServiceV1.resetPassword(code, request.getNewPassword(), request.getConfirmPassword()));
    }
}