package com.sparta.todayeats.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ResetPasswordResponse {
    private String email;
    private LocalDateTime updatedAt;
}