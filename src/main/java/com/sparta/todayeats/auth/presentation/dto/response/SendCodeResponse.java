package com.sparta.todayeats.auth.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SendCodeResponse {
    private String email;
    private LocalDateTime expiredAt;
}