package com.sparta.todayeats.user.dto.request;

import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateRoleRequest {
    @NotNull(message = "사용자 권한은 필수입니다.")
    private UserRoleEnum role;
}