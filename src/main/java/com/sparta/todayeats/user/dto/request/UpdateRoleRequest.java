package com.sparta.todayeats.user.dto.request;

import com.sparta.todayeats.user.entity.UserRoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Schema(description = "사용자 권한 변경 요청")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateRoleRequest {
    @Schema(description = "권한", example = "MANAGER")
    @NotNull(message = "사용자 권한은 필수입니다.")
    private UserRoleEnum role;
}