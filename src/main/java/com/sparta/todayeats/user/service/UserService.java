package com.sparta.todayeats.user.service;

import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.UserErrorCode;
import com.sparta.todayeats.global.service.UserAuthorizationService;
import com.sparta.todayeats.global.util.PageableUtils;
import com.sparta.todayeats.user.domain.entity.User;
import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import com.sparta.todayeats.user.domain.repository.UserRepository;
import com.sparta.todayeats.user.dto.request.UpdatePasswordRequest;
import com.sparta.todayeats.user.dto.request.UpdateUserRequest;
import com.sparta.todayeats.user.dto.response.UpdatePasswordResponse;
import com.sparta.todayeats.user.dto.response.UpdateUserResponse;
import com.sparta.todayeats.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserAuthorizationService userAuthorizationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponse> searchUsers(
            String keyword, UserRoleEnum role, Pageable pageable, UUID currentUserId
    ) {
        // 관리자 검증: DB 조회
        User currentUser = userAuthorizationService.getUserById(currentUserId);
        userAuthorizationService.validateAdmin(currentUser);

        // 페이지 크기 검증
        PageableUtils.checkSize(pageable);

        // 사용자 조회
        Page<User> userPage = userRepository.searchUsers(keyword, role, pageable);

        return userPage.map(user -> new UserResponse(user, true));
    }

    public UserResponse findUser(UUID targetUserId, UUID currentUserId) {
        // 대상 사용자 조회
        User targetUser = userAuthorizationService.getUserById(targetUserId);
        User currentUser = userAuthorizationService.getUserById(currentUserId);

        // 타인 조회: 관리자만 가능
        if (!targetUserId.equals(currentUserId)) {
            userAuthorizationService.validateAdmin(currentUser);
        }

        // 권한에 맞는 결과 반환
        return new UserResponse(targetUser, userAuthorizationService.isAdmin(currentUser));
    }

    @Transactional
    public UpdateUserResponse updateUser(UpdateUserRequest request, UUID userId) {
        // 사용자 조회
        User user = userAuthorizationService.getUserById(userId);

        // 사용자 수정
        user.update(request.getNickname(), request.isVisible());

        return new UpdateUserResponse(user);
    }

    @Transactional
    public UpdatePasswordResponse updatePassword(UpdatePasswordRequest request, UUID userId) {
        // 사용자 조회
        User user = userAuthorizationService.getUserById(userId);

        // 현재 비밀번호 일치 확인
        String currentPassword = request.getCurrentPassword();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BaseException(UserErrorCode.PASSWORD_MISMATCH);
        }

        // 새 비밀번호 일치 확인
        String newPassword = request.getNewPassword();
        if (!newPassword.equals(request.getConfirmNewPassword())) {
            throw new BaseException(UserErrorCode.PASSWORD_MISMATCH);
        }

        // 현재 비밀번호와 새 비밀번호 일치 확인
        if (currentPassword.equals(newPassword)) {
            throw new BaseException(UserErrorCode.DUPLICATE_PASSWORD);
        }

        // 비밀번호 변경
        user.updatePassword(passwordEncoder.encode(newPassword));

        return new UpdatePasswordResponse(user);
    }
}