package com.sparta.todayeats.user.controller;

import com.sparta.todayeats.global.annotation.ApiPageable;
import com.sparta.todayeats.global.annotation.LoginUser;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.user.entity.UserRoleEnum;
import com.sparta.todayeats.user.dto.request.UpdatePasswordRequest;
import com.sparta.todayeats.user.dto.request.UpdateRoleRequest;
import com.sparta.todayeats.user.dto.request.UpdateUserRequest;
import com.sparta.todayeats.user.dto.response.*;
import com.sparta.todayeats.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "사용자 목록 조회")
    @ApiPageable
    @Secured({"ROLE_MANAGER", "ROLE_MASTER"})
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @Parameter(description = "이메일 또는 닉네임", example = "홍길동")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "권한 필터", example = "OWNER")
            @RequestParam(required = false) UserRoleEnum role,
            @Parameter(hidden = true)
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(hidden = true) @LoginUser UUID currentUserId
    ) {
        PageResponse<UserResponse> response = userService.searchUsers(keyword, role, pageable, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 상세 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> findUser(
            @Parameter(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable("userId") UUID targetUserId,
            @Parameter(hidden = true) @LoginUser UUID currentUserId
    ) {
        UserResponse response = userService.findUser(targetUserId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 수정")
    @PutMapping
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateUser(
            @Valid @RequestBody UpdateUserRequest request,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {
        UpdateUserResponse response = userService.updateUser(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 비밀번호 변경")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<UpdatePasswordResponse>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {
        UpdatePasswordResponse response = userService.updatePassword(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 권한 변경")
    @Secured("ROLE_MASTER")
    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UpdateRoleResponse>> updateRole(
            @Parameter(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable("userId") UUID targetUserId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        UpdateRoleResponse response = userService.updateRole(targetUserId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 삭제")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<?>> deleteUser(
            @Parameter(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable("userId") UUID targetUserId,
            @Parameter(hidden = true) @LoginUser UUID currentUserId
    ) {
        DeleteUserResponse response = userService.deleteUser(targetUserId, currentUserId);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}