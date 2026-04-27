package com.sparta.todayeats.user.controller;

import com.sparta.todayeats.global.annotation.LoginUser;
import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import com.sparta.todayeats.user.dto.request.UpdatePasswordRequest;
import com.sparta.todayeats.user.dto.request.UpdateRoleRequest;
import com.sparta.todayeats.user.dto.request.UpdateUserRequest;
import com.sparta.todayeats.user.dto.response.*;
import com.sparta.todayeats.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRoleEnum role,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @LoginUser UUID currentUserId
    ) {
        Page<UserResponse> response = userService.searchUsers(keyword, role, pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{targetUserId}")
    public ResponseEntity<UserResponse> findUser(@PathVariable UUID targetUserId, @LoginUser UUID currentUserId) {
        return ResponseEntity.ok(userService.findUser(targetUserId, currentUserId));
    }

    @PutMapping
    public ResponseEntity<UpdateUserResponse> updateUser(
            @Valid @RequestBody UpdateUserRequest request, @LoginUser UUID userId
    ) {
        return ResponseEntity.ok(userService.updateUser(request, userId));
    }

    @PatchMapping("/password")
    public ResponseEntity<UpdatePasswordResponse> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request, @LoginUser UUID userId
    ) {
        return ResponseEntity.ok(userService.updatePassword(request, userId));
    }

    @Secured("ROLE_MASTER")
    @PatchMapping("/{targetUserId}/role")
    public ResponseEntity<UpdateRoleResponse> updateRole(
            @PathVariable UUID targetUserId, @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(userService.updateRole(targetUserId, request));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID targetUserId, @LoginUser UUID currentUserId) {
        DeleteUserResponse response = userService.deleteUser(targetUserId, currentUserId);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}