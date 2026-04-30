package com.sparta.todayeats.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "인증번호 응답")
@Getter
@AllArgsConstructor
public class CodeResponse {
    @Schema(description = "이메일", example = "user01@example.com")
    private String email;

    @Schema(description = "만료 시간", example = "2026-04-30T10:00:00")
    private LocalDateTime expiredAt;
}