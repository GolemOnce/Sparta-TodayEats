package com.sparta.todayeats.auth.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private UUID userId;
    private String nickname;
    private String role;
    private String accessToken;
    private String refreshToken;
}