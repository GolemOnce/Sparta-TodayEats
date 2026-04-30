package com.sparta.todayeats.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Schema(description = "사용자 비밀번호 변경 요청")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdatePasswordRequest {
    @Schema(description = "현재 비밀번호", example = "Password1!")
    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    @Schema(description = "새 비밀번호", example = "Password249!")
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
            message = "비밀번호는 8~15자이며, 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String newPassword;

    @Schema(description = "새 비밀번호 확인", example = "Password249!")
    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    private String confirmNewPassword;
}