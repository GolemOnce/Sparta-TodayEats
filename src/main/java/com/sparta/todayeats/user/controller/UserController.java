package com.sparta.todayeats.user.controller;

import com.sparta.todayeats.global.annotation.LoginUser;
import com.sparta.todayeats.global.response.ApiResponse;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.user.entity.UserRoleEnum;
import com.sparta.todayeats.user.dto.request.UpdatePasswordRequest;
import com.sparta.todayeats.user.dto.request.UpdateRoleRequest;
import com.sparta.todayeats.user.dto.request.UpdateUserRequest;
import com.sparta.todayeats.user.dto.response.*;
import com.sparta.todayeats.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Secured({"ROLE_MANAGER", "ROLE_MASTER"})
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRoleEnum role,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @LoginUser UUID currentUserId
    ) {
        PageResponse<UserResponse> response = userService.searchUsers(keyword, role, pageable, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<UserResponse>> findUser(@PathVariable UUID targetUserId, @LoginUser UUID currentUserId) {
        UserResponse response = userService.findUser(targetUserId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateUser(
            @Valid @RequestBody UpdateUserRequest request, @LoginUser UUID userId
    ) {
        UpdateUserResponse response = userService.updateUser(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<UpdatePasswordResponse>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request, @LoginUser UUID userId
    ) {
        UpdatePasswordResponse response = userService.updatePassword(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Secured("ROLE_MASTER")
    @PatchMapping("/{targetUserId}/role")
    public ResponseEntity<ApiResponse<UpdateRoleResponse>> updateRole(
            @PathVariable UUID targetUserId, @Valid @RequestBody UpdateRoleRequest request
    ) {
        UpdateRoleResponse response = userService.updateRole(targetUserId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable UUID targetUserId, @LoginUser UUID currentUserId) {
        DeleteUserResponse response = userService.deleteUser(targetUserId, currentUserId);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}