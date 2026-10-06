package com.sparta.todayeats.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Schema(description = "사용자 정보 수정 요청")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateUserRequest {
    @Schema(description = "닉네임", example = "홍길동")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;

    @Schema(description = "정보 공개 여부", example = "false")
    @NotNull(message = "정보 공개 여부는 필수입니다.")
    private boolean visible;
}