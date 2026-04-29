package com.sparta.todayeats.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConfirmCodeResponse {
    private String email;
}