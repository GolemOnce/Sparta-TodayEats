package com.sparta.todayeats.address.service;

import com.sparta.todayeats.address.dto.reqeust.AddressCreateRequest;
import com.sparta.todayeats.address.dto.response.AddressCreateResponse;
import com.sparta.todayeats.address.dto.response.AddressPageResponse;
import com.sparta.todayeats.address.entity.Address;
import com.sparta.todayeats.address.repository.AddressRepository;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.user.domain.entity.User;
import com.sparta.todayeats.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class AddressServiceTest {
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AddressService addressService;

    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Mock 데이터
        mockUser = mock(User.class);
        given(mockUser.getUserId()).willReturn(userId);
    }
    @Nested
    @DisplayName("배송지 등록")
    class createAddress {

        @Test
        void 배송지_등록_성공() {
            // given
            AddressCreateRequest request = new AddressCreateRequest("집", "서울시 강남구", "101호", "12345");

            User user = mock(User.class);
            given(user.getUserId()).willReturn(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            Address address = Address.builder()
                    .user(user)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            given(addressRepository.save(any(Address.class))).willAnswer(i -> i.getArgument(0));

            // when
            AddressCreateResponse response = addressService.createAddress(userId, request);

            // then
            assertThat(response.getAlias()).isEqualTo("집");
            assertThat(response.getAddress()).isEqualTo("서울시 강남구");
            assertThat(response.getDetail()).isEqualTo("101호");
            assertThat(response.getZipCode()).isEqualTo("12345");
            assertThat(response.isDefault()).isFalse();
            verify(addressRepository).save(any(Address.class));
        }

        @Test
        void 존재하지_않는_유저_등록시_예외() {
            // given
            AddressCreateRequest request = new AddressCreateRequest("집", "서울시 강남구", "101호", "12345");
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> addressService.createAddress(userId, request))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("배송지 목록 조회")
    class getPagedAddresses {

        @Test
        void 배송지_목록_조회_성공() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            User user = mock(User.class);
            given(user.getUserId()).willReturn(userId);

            Address address = Address.builder()
                    .user(user)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            Page<Address> addressPage = new PageImpl<>(List.of(address), pageable, 1);
            given(userRepository.existsById(userId)).willReturn(true);
            given(addressRepository.findByUserId(userId, pageable)).willReturn(addressPage);

            // when
            AddressPageResponse response = addressService.getPagedAddresses(userId, pageable);

            // then
            assertThat(response.getAddresses().size()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
            verify(addressRepository).findByUserId(userId, pageable);
        }

        @Test
        void 존재하지_않는_유저_조회시_예외() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            given(userRepository.existsById(userId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> addressService.getPagedAddresses(userId, pageable))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 배송지_없으면_빈_페이지_반환() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            given(userRepository.existsById(userId)).willReturn(true);
            given(addressRepository.findByUserId(userId, pageable)).willReturn(Page.empty(pageable));

            // when
            AddressPageResponse response = addressService.getPagedAddresses(userId, pageable);

            // then
            assertThat(response.getAddresses().size()).isEqualTo(0);
            assertThat(response.getTotalElements()).isEqualTo(0);
        }
    }

}