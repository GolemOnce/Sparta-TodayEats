package com.sparta.todayeats.user.repository;

import com.sparta.todayeats.global.infrastructure.config.JpaConfig;
import com.sparta.todayeats.user.entity.User;
import com.sparta.todayeats.user.entity.UserRoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(user("master", "마스터", UserRoleEnum.MASTER));
        userRepository.save(user("customer1", "고객1", UserRoleEnum.CUSTOMER));
        userRepository.save(user("customer2", "고객2", UserRoleEnum.CUSTOMER));
        userRepository.save(user("customer3", "고객3", UserRoleEnum.CUSTOMER));
        userRepository.save(user("owner1", "점주1", UserRoleEnum.OWNER));
        userRepository.save(user("owner2", "점주2", UserRoleEnum.OWNER));
        userRepository.save(user("manager", "매니저", UserRoleEnum.MANAGER));
    }

    private User user(String email, String nickname, UserRoleEnum role) {
        return User.builder()
                .email(email + "@test.com")
                .password("password123!")
                .nickname(nickname)
                .role(role)
                .build();
    }

    @Test
    void 전체_사용자_조회() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result = userRepository.searchUsers(null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(7);
    }

    @Test
    void 사용자_검색_이메일_키워드() {
        // given
        String keyword = "customer1";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result = userRepository.searchUsers(keyword, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).contains(keyword);
    }

    @Test
    void 사용자_검색_닉네임_키워드() {
        // given
        String keyword = "고객";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result = userRepository.searchUsers(keyword, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(user -> user.getNickname().startsWith(keyword));
    }

    @Test
    void 사용자_검색_권한() {
        // given
        UserRoleEnum role = UserRoleEnum.CUSTOMER;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result = userRepository.searchUsers(null, role, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).allMatch(user -> user.getRole() == role);
    }

    @Test
    void 사용자_검색_키워드_권한() {
        // given
        String keyword = "test";
        UserRoleEnum role = UserRoleEnum.MASTER;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result = userRepository.searchUsers(keyword, role, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("master@test.com");
    }

    @Test
    void 전체_사용자_조회_생성일_내림차순() throws InterruptedException {
        // given
        Thread.sleep(10);

        userRepository.save(user("new", "신규", UserRoleEnum.CUSTOMER));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<User> result = userRepository.searchUsers(null, null, pageable);

        // then
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void 사용자_검색_결과_없음_키워드_불일치() {
        // given
        String keyword = "존재하지 않는 키워드";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result = userRepository.searchUsers(keyword, null, pageable);

        // then
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void 사용자_검색_결과_없음_키워드_일치_권한_불일치() {
        // given
        String keyword = "master";
        UserRoleEnum role = UserRoleEnum.CUSTOMER;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result = userRepository.searchUsers(keyword, role, pageable);

        // then
        assertThat(result.isEmpty()).isTrue();
    }
}