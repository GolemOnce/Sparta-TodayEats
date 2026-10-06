package com.sparta.todayeats.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Schema(description = "로그인 요청")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginRequest {
    @Schema(description = "이메일", example = "user01@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @Schema(description = "비밀번호", example = "Password1!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}