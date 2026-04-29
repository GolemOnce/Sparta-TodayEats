package com.sparta.todayeats.user.service;

import com.sparta.todayeats.auth.application.service.AuthService;
import com.sparta.todayeats.global.exception.AuthErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.CommonErrorCode;
import com.sparta.todayeats.global.exception.UserErrorCode;
import com.sparta.todayeats.global.service.UserAuthorizationService;
import com.sparta.todayeats.order.service.OrderService;
import com.sparta.todayeats.user.entity.User;
import com.sparta.todayeats.user.entity.UserRoleEnum;
import com.sparta.todayeats.user.repository.UserRepository;
import com.sparta.todayeats.user.dto.request.UpdatePasswordRequest;
import com.sparta.todayeats.user.dto.request.UpdateRoleRequest;
import com.sparta.todayeats.user.dto.request.UpdateUserRequest;
import com.sparta.todayeats.user.dto.response.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @Mock
    private AuthService authService;

    @Mock
    private OrderService orderService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static final UUID CURRENT_USER_ID = UUID.randomUUID();
    private static final UUID TARGET_USER_ID = UUID.randomUUID();
    private static final String ENCODED_PASSWORD = "encoded-old";

    private User user(UserRoleEnum role) {
        return User.builder().role(role).password(ENCODED_PASSWORD).build();
    }

    @Nested
    @DisplayName("searchUsers()")
    class SearchUsers {
        private final String KEYWORD = "test";
        private final UserRoleEnum ROLE = UserRoleEnum.CUSTOMER;

        @Test
        void 사용자_검색_성공() {
            // given
            User currentUser = user(UserRoleEnum.MASTER);
            User targetUser = user(UserRoleEnum.CUSTOMER);
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(targetUser));

            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(currentUser);
            given(userRepository.searchUsers(KEYWORD, ROLE, pageable)).willReturn(userPage);

            // when
            Page<UserResponse> responsePage = userService.searchUsers(KEYWORD, ROLE, pageable, CURRENT_USER_ID);

            // then
            assertThat(responsePage).isNotNull();
            assertThat(responsePage.getContent().get(0).getRole()).isEqualTo(targetUser.getRole().toString());
            verify(userAuthorizationService).validateAdmin(currentUser);
            verify(userRepository).searchUsers(KEYWORD, ROLE, pageable);
        }

        @Test
        void 사용자_검색_실패_비관리자() {
            // given
            User user = user(UserRoleEnum.CUSTOMER);
            Pageable pageable = PageRequest.of(0, 10);

            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);
            doThrow(new BaseException(AuthErrorCode.FORBIDDEN))
                    .when(userAuthorizationService).validateAdmin(user);

            // when & then
            assertThatThrownBy(() -> userService.searchUsers(KEYWORD, ROLE, pageable, CURRENT_USER_ID))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(AuthErrorCode.FORBIDDEN.getMessage());
        }

        @Test
        void 사용자_검색_실패_페이지_크기_미유효() {
            // given
            User user = user(UserRoleEnum.MASTER);
            Pageable pageable = PageRequest.of(0, 20);

            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);

            // when & then
            assertThatThrownBy(() -> userService.searchUsers(KEYWORD, ROLE, pageable, CURRENT_USER_ID))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(CommonErrorCode.INVALID_PAGE_SIZE.getMessage());
        }
    }

    @Nested
    @DisplayName("findUser()")
    class FindUser {
        @Test
        void 사용자_조회_성공_본인() {
            // given
            User user = user(UserRoleEnum.CUSTOMER);
            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);
            given(userAuthorizationService.isAdmin(user)).willReturn(false);

            // when
            UserResponse response = userService.findUser(CURRENT_USER_ID, CURRENT_USER_ID);

            // then
            assertThat(response.getRole()).isEqualTo(user.getRole().toString());
            verify(userAuthorizationService, never()).validateAdmin(user);
        }

        @Test
        void 사용자_조회_성공_타인() {
            // given
            User targetUser = user(UserRoleEnum.CUSTOMER);
            User currentUser = user(UserRoleEnum.MASTER);

            given(userAuthorizationService.getUserById(TARGET_USER_ID)).willReturn(targetUser);
            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(currentUser);
            given(userAuthorizationService.isAdmin(currentUser)).willReturn(true);

            // when
            UserResponse response = userService.findUser(TARGET_USER_ID, CURRENT_USER_ID);

            // then
            assertThat(response.getRole()).isEqualTo(targetUser.getRole().toString());
            verify(userAuthorizationService).validateAdmin(currentUser);
        }

        @Test
        void 사용자_조회_실패_타인() {
            // given
            User targetUser = user(UserRoleEnum.CUSTOMER);
            User currentUser = user(UserRoleEnum.CUSTOMER);

            given(userAuthorizationService.getUserById(TARGET_USER_ID)).willReturn(targetUser);
            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(currentUser);
            doThrow(new BaseException(AuthErrorCode.FORBIDDEN))
                    .when(userAuthorizationService).validateAdmin(currentUser);

            // when & then
            assertThatThrownBy(() -> userService.findUser(TARGET_USER_ID, CURRENT_USER_ID))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(AuthErrorCode.FORBIDDEN.getMessage());
        }
    }

    @Test
    @DisplayName("updateUser()")
    void 사용자_수정_성공() {
        UpdateUserRequest request = new UpdateUserRequest("테스터", false);
        User user = user(UserRoleEnum.CUSTOMER);

        given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);

        // when
        UpdateUserResponse response = userService.updateUser(request, CURRENT_USER_ID);

        // then
        assertThat(response.getNickname()).isEqualTo(request.getNickname());
        assertThat(response.isVisible()).isFalse();
        verify(userAuthorizationService, never()).validateAdmin(user);
    }

    @Nested
    @DisplayName("updatePassword()")
    class UpdatePassword {
        private final String WRONG_PASSWORD = "wrong";
        private final String PASSWORD = "decoded-old";
        private final String NEW_PASSWORD = "decoded-new";

        @Test
        void 비밀번호_변경_성공() {
            // given
            UpdatePasswordRequest request = new UpdatePasswordRequest(PASSWORD, NEW_PASSWORD, NEW_PASSWORD);
            User user = user(UserRoleEnum.CUSTOMER);

            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);
            given(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);
            given(passwordEncoder.encode(NEW_PASSWORD)).willReturn("encoded-new");

            // when
            UpdatePasswordResponse response = userService.updatePassword(request, CURRENT_USER_ID);

            // then
            assertThat(response).isNotNull();
            verify(passwordEncoder).encode(NEW_PASSWORD);
        }

        @Test
        void 비밀번호_변경_실패_현재_비밀번호_불일치() {
            // given
            UpdatePasswordRequest request = new UpdatePasswordRequest(WRONG_PASSWORD, NEW_PASSWORD, NEW_PASSWORD);
            User user = user(UserRoleEnum.CUSTOMER);

            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);
            given(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.updatePassword(request, CURRENT_USER_ID))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.PASSWORD_MISMATCH.getMessage());
        }

        @Test
        void 비밀번호_변경_실패_새_비밀번호_불일치() {
            // given
            UpdatePasswordRequest request = new UpdatePasswordRequest(PASSWORD, NEW_PASSWORD, WRONG_PASSWORD);
            User user = user(UserRoleEnum.CUSTOMER);

            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);
            given(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.updatePassword(request, CURRENT_USER_ID))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.PASSWORD_MISMATCH.getMessage());
        }

        @Test
        void 비밀번호_변경_실패_동일_비밀번호() {
            // given
            UpdatePasswordRequest request = new UpdatePasswordRequest(PASSWORD, PASSWORD, PASSWORD);
            User user = user(UserRoleEnum.CUSTOMER);

            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);
            given(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.updatePassword(request, CURRENT_USER_ID))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.DUPLICATE_PASSWORD.getMessage());
        }
    }

    @Nested
    @DisplayName("updateRole()")
    class UpdateRole {
        @Test
        void 권한_변경_성공() {
            // given
            UpdateRoleRequest request = new UpdateRoleRequest(UserRoleEnum.MANAGER);
            User targetUser = user(UserRoleEnum.CUSTOMER);

            given(userAuthorizationService.getUserById(TARGET_USER_ID)).willReturn(targetUser);
            given(userAuthorizationService.isMaster(targetUser)).willReturn(false);

            // when
            UpdateRoleResponse response = userService.updateRole(TARGET_USER_ID, request);

            // then
            assertThat(response.getRole()).isEqualTo(request.getRole().toString());
        }

        @Test
        void 권한_변경_실패_대상_Master() {
            // given
            UpdateRoleRequest request = new UpdateRoleRequest(UserRoleEnum.MANAGER);
            User targetUser = user(UserRoleEnum.MASTER);

            given(userAuthorizationService.getUserById(TARGET_USER_ID)).willReturn(targetUser);
            given(userAuthorizationService.isMaster(targetUser)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.updateRole(TARGET_USER_ID, request))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.CANNOT_UPDATE_MASTER_ROLE.getMessage());
        }

        @Test
        void 권한_변경_실패_Master_부여() {
            // given
            UpdateRoleRequest request = new UpdateRoleRequest(UserRoleEnum.MASTER);
            User targetUser = user(UserRoleEnum.CUSTOMER);

            given(userAuthorizationService.getUserById(TARGET_USER_ID)).willReturn(targetUser);
            given(userAuthorizationService.isMaster(targetUser)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.updateRole(TARGET_USER_ID, request))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.CANNOT_GRANT_MASTER_ROLE.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {
        @Test
        void 사용자_삭제_성공_본인() {
            // given
            User user = user(UserRoleEnum.CUSTOMER);

            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);
            given(userAuthorizationService.isMaster(user)).willReturn(false);
            given(userAuthorizationService.isAdmin(user)).willReturn(false);
            given(orderService.hasActiveOrders(CURRENT_USER_ID)).willReturn(false);

            // when
            DeleteUserResponse response = userService.deleteUser(CURRENT_USER_ID, CURRENT_USER_ID);

            // then
            assertThat(response).isNull();
            assertThat(user.getDeletedAt()).isNotNull();
            verify(authService).deleteRefreshToken(CURRENT_USER_ID.toString());
        }

        @Test
        void 사용자_삭제_성공_타인() {
            // given
            User targetUser = user(UserRoleEnum.CUSTOMER);
            User currentUser = user(UserRoleEnum.MASTER);

            given(userAuthorizationService.getUserById(TARGET_USER_ID)).willReturn(targetUser);
            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(currentUser);
            given(userAuthorizationService.isMaster(currentUser)).willReturn(true);
            given(userAuthorizationService.isMaster(targetUser)).willReturn(false);

            // when
            DeleteUserResponse response = userService.deleteUser(TARGET_USER_ID, CURRENT_USER_ID);

            // then
            assertThat(response.getDeletedAt()).isNotNull();
            assertThat(targetUser.getDeletedAt()).isNotNull();
            assertThat(currentUser.getDeletedAt()).isNull();
            verify(authService).deleteRefreshToken(TARGET_USER_ID.toString());
        }

        @Test
        void 사용자_삭제_실패_대상_Master() {
            // given
            User user = user(UserRoleEnum.MASTER);

            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);
            given(userAuthorizationService.isMaster(user)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(CURRENT_USER_ID, CURRENT_USER_ID))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.CANNOT_DELETE_MASTER.getMessage());
        }

        @Test
        void 사용자_삭제_실패_대상_주문_있음() {
            // given
            User user = user(UserRoleEnum.CUSTOMER);

            given(userAuthorizationService.getUserById(CURRENT_USER_ID)).willReturn(user);
            given(userAuthorizationService.isMaster(user)).willReturn(false);
            given(userAuthorizationService.isAdmin(user)).willReturn(false);
            given(orderService.hasActiveOrders(CURRENT_USER_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(CURRENT_USER_ID, CURRENT_USER_ID))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.ACTIVE_ORDER_EXISTS.getMessage());
        }
    }
}