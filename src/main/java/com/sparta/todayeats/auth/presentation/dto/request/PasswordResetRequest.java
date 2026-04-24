package com.sparta.todayeats.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordResetRequest {
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
            message = "비밀번호는 8~15자이며, 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String newPassword;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword;
}