package com.sparta.todayeats.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "인증번호 확인 응답")
@Getter
@AllArgsConstructor
public class ConfirmCodeResponse {
    @Schema(description = "이메일", example = "user01@example.com")
    private String email;
}