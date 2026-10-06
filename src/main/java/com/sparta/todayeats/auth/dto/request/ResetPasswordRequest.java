package com.sparta.todayeats.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Schema(description = "비밀번호 재설정 요청")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResetPasswordRequest {
    @Schema(description = "비밀번호", example = "Password1!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
            message = "비밀번호는 8~15자이며, 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String newPassword;

    @Schema(description = "비밀번호 확인", example = "Password1!")
    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmNewPassword;
}