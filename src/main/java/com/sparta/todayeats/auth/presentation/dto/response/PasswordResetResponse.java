package com.sparta.todayeats.auth.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PasswordResetResponse {
    private String email;
    private LocalDateTime updatedAt;
}