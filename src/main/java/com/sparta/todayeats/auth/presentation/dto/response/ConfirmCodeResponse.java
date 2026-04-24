package com.sparta.todayeats.auth.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConfirmCodeResponse {
    private String email;
}