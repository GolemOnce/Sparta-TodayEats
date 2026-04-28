package com.sparta.todayeats.auth.presentation.controller;

import com.sparta.todayeats.auth.application.service.AuthService;
import com.sparta.todayeats.auth.presentation.dto.request.*;
import com.sparta.todayeats.auth.presentation.dto.response.*;
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
    public ResponseEntity<CodeResponse> sendSignupCode(@Valid @RequestBody SendCodeRequest request) {
        return ResponseEntity.ok(authService.sendSignupCode(request.getEmail()));
    }

    @PostMapping("/verify-code/confirm")
    public ResponseEntity<CodeResponse> confirmSignupCode(@Valid @RequestBody ConfirmCodeRequest request) {
        return ResponseEntity.ok(authService.confirmSignupCode(request.getEmail(), request.getCode()));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.reissue(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password/send")
    public ResponseEntity<CodeResponse> sendPasswordResetLink(@Valid @RequestBody SendCodeRequest request) {
        return ResponseEntity.ok(authService.sendPasswordResetLink(request.getEmail()));
    }

    @GetMapping("/reset-password")
    public ResponseEntity<ConfirmCodeResponse> confirmPasswordResetLink(@RequestParam String code) {
        return ResponseEntity.ok(authService.confirmPasswordResetLink(code));
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @RequestParam String code, @Valid @RequestBody ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(authService.resetPassword(
                code, request.getNewPassword(), request.getConfirmNewPassword())
        );
    }
}