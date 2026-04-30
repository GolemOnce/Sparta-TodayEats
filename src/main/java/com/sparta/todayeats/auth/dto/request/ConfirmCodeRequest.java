package com.sparta.todayeats.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Schema(description = "인증번호 확인 요청")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfirmCodeRequest {
    @Schema(description = "이메일", example = "user01@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "인증번호", example = "123456")
    @NotBlank(message = "인증번호는 필수입니다.")
    @Pattern(regexp = "\\d{6}", message = "인증번호는 6자리 숫자입니다.")
    private String code;
}