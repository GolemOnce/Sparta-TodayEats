package com.sparta.todayeats.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CodeResponse {
    private String email;
    private LocalDateTime expiredAt;
}