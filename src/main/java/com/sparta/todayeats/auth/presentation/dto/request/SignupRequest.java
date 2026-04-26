package com.sparta.todayeats.auth.presentation.dto.request;

import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequest {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
            message = "비밀번호는 8~15자이며, 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;

    @NotNull(message = "회원 유형 선택은 필수입니다.")
    private UserRoleEnum role;

    @AssertTrue(message = "가입 가능한 회원 유형이 아닙니다.")
    public boolean isValidRole() {
        return role == UserRoleEnum.CUSTOMER || role == UserRoleEnum.OWNER;
    }

    public void encodePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}