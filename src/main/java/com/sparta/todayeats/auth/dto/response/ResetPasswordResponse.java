package com.sparta.todayeats.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "비밀번호 재설정 응답")
@Getter
@AllArgsConstructor
public class ResetPasswordResponse {
    @Schema(description = "이메일", example = "user01@example.com")
    private String email;

    @Schema(description = "수정 시간", example = "2026-04-30T12:00:00")
    private LocalDateTime updatedAt;
}