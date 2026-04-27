package com.sparta.todayeats.global.service;

import com.sparta.todayeats.global.exception.AuthErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.UserErrorCode;
import com.sparta.todayeats.user.entity.User;
import com.sparta.todayeats.user.entity.UserRoleEnum;
import com.sparta.todayeats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserAuthorizationService {
    private final UserRepository userRepository;

    public User getUserById(UUID userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
    }

    // 권한 검증
    public void validateMaster(User user) {
        if (!isMaster(user)) {
            throw new BaseException(AuthErrorCode.FORBIDDEN);
        }
    }

    public void validateAdmin(User user) {
        if (!isAdmin(user)) {
            throw new BaseException(AuthErrorCode.FORBIDDEN);
        }
    }

    public void validateSelf(UUID targetUserId, UUID currentUserId) {
        if (!targetUserId.equals(currentUserId)) {
            throw new BaseException(AuthErrorCode.FORBIDDEN);
        }
    }

    // 권한 확인
    public boolean isOwner(User user) {
        return user.getRole() == UserRoleEnum.OWNER;
    }

    public boolean isMaster(User user) {
        return user.getRole() == UserRoleEnum.MASTER;
    }

    public boolean isManager(User user) {
        return user.getRole() == UserRoleEnum.MANAGER;
    }

    public boolean isAdmin(User user) {
        return isMaster(user) || isManager(user);
    }
}