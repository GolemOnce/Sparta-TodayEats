package com.sparta.todayeats.user.entity;

import com.sparta.todayeats.auth.presentation.dto.request.SignupRequest;
import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity @Table(name = "p_user")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRoleEnum role;

    @Column(nullable = false)
    private boolean isPublic = true;

    @Builder
    public User(String email, String password, String nickname, UserRoleEnum role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public void update(String nickname, boolean visible) {
        this.nickname = nickname;
        this.isPublic = visible;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateRole(UserRoleEnum role) {
        this.role = role;
    }

    public void restore(SignupRequest request) {
        this.password = request.getPassword();
        this.nickname = request.getNickname();
        this.role = request.getRole();
        this.restore();
    }
}