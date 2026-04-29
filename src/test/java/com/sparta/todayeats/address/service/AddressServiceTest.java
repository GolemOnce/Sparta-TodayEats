package com.sparta.todayeats.address.service;

import com.sparta.todayeats.address.dto.request.AddressCreateRequest;
import com.sparta.todayeats.address.dto.request.AddressUpdateRequest;
import com.sparta.todayeats.address.dto.response.*;
import com.sparta.todayeats.address.entity.Address;
import com.sparta.todayeats.address.repository.AddressRepository;
import com.sparta.todayeats.global.exception.AuthErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.global.service.UserAuthorizationService;
import com.sparta.todayeats.user.entity.User;
import com.sparta.todayeats.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AddressService addressService;
    @Mock
    private UserAuthorizationService userAuthorizationService;
    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Mock 데이터
        mockUser = mock(User.class);
    }
    @Nested
    @DisplayName("배송지 등록")
    class createAddress {

        @Test
        void 배송지_등록_성공() {
            // given
            AddressCreateRequest request = new AddressCreateRequest("집", "서울시 강남구", "101호", "12345");

            User user = mock(User.class);
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
            PageResponse<AddressResponse> response = addressService.getPagedAddresses(userId, pageable);

            // then
            assertThat(response.getContent().size()).isEqualTo(1);
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
            PageResponse<AddressResponse> response = addressService.getPagedAddresses(userId, pageable);

            // then
            assertThat(response.getContent().size()).isEqualTo(0);
            assertThat(response.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("배송지 상세 조회")
    class getDetailAddress {

        @Test
        void 배송지_상세_조회_성공() {
            // given
            UUID addressId = UUID.randomUUID();

            given(mockUser.getUserId()).willReturn(userId);

            Address address = Address.builder()
                    .user(mockUser)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when
            AddressDetailResponse response = addressService.getDetailAddress(userId, addressId);

            // then
            assertThat(response.getAlias()).isEqualTo("집");
            assertThat(response.getAddress()).isEqualTo("서울시 강남구");
            verify(addressRepository).findById(addressId);
        }

        @Test
        void 존재하지_않는_배송지_조회시_예외() {
            // given
            UUID addressId = UUID.randomUUID();
            given(addressRepository.findById(addressId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> addressService.getDetailAddress(userId, addressId))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 본인_배송지가_아니면_예외() {
            // given
            UUID addressId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            given(mockUser.getUserId()).willReturn(userId);

            Address address = Address.builder()
                    .user(mockUser)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));
            doThrow(new BaseException(AuthErrorCode.FORBIDDEN))
                    .when(userAuthorizationService).validateSelf(otherUserId, userId);

            // when & then
            assertThatThrownBy(() -> addressService.getDetailAddress(otherUserId, addressId))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("배송지 수정")
    class updateAddress {

        @Test
        void 배송지_수정_성공() {
            // given
            UUID addressId = UUID.randomUUID();
            AddressUpdateRequest request = new AddressUpdateRequest("회사", "서울시 서초구", "202호", "54321");

            given(mockUser.getUserId()).willReturn(userId);

            Address address = Address.builder()
                    .user(mockUser)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when
            AddressUpdateResponse response = addressService.updateAddress(userId, addressId, request);

            // then
            assertThat(response.getAlias()).isEqualTo("회사");
            assertThat(response.getAddress()).isEqualTo("서울시 서초구");
        }

        @Test
        void 존재하지_않는_배송지_수정시_예외() {
            // given
            UUID addressId = UUID.randomUUID();
            AddressUpdateRequest request = new AddressUpdateRequest("회사", "서울시 서초구", "202호", "54321");
            given(addressRepository.findById(addressId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> addressService.updateAddress(userId, addressId, request))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 본인_배송지가_아니면_예외() {
            // given
            UUID addressId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            AddressUpdateRequest request = new AddressUpdateRequest("회사", "서울시 서초구", "202호", "54321");

            given(mockUser.getUserId()).willReturn(userId);

            Address address = Address.builder()
                    .user(mockUser)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));
            doThrow(new BaseException(AuthErrorCode.FORBIDDEN))
                    .when(userAuthorizationService).validateSelf(otherUserId, userId);

            // when & then
            assertThatThrownBy(() -> addressService.updateAddress(otherUserId, addressId, request))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("기본 배송지 설정")
    class setDefaultAddress {

        @Test
        void 기본_배송지_설정_성공() {
            // given
            UUID addressId = UUID.randomUUID();

            given(mockUser.getUserId()).willReturn(userId);

            Address address = Address.builder()
                    .user(mockUser)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            given(addressRepository.findByUserUserIdAndIsDefaultTrue(userId)).willReturn(Optional.empty());
            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when
            AddressDefaultResponse response = addressService.setDefaultAddress(userId, addressId);

            // then
            assertThat(response.isDefault()).isTrue();
        }

        @Test
        void 기존_기본_배송지가_있으면_false로_변경() {
            // given
            UUID addressId = UUID.randomUUID();

            given(mockUser.getUserId()).willReturn(userId);

            Address existingDefault = Address.builder()
                    .user(mockUser)
                    .alias("회사")
                    .address("서울시 서초구")
                    .detail("202호")
                    .zipCode("54321")
                    .isDefault(true)
                    .build();

            Address newDefault = Address.builder()
                    .user(mockUser)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            given(addressRepository.findByUserUserIdAndIsDefaultTrue(userId)).willReturn(Optional.of(existingDefault));
            given(addressRepository.findById(addressId)).willReturn(Optional.of(newDefault));

            // when
            addressService.setDefaultAddress(userId, addressId);

            // then
            assertThat(existingDefault.isDefault()).isFalse();
            assertThat(newDefault.isDefault()).isTrue();
        }

        @Test
        void 존재하지_않는_배송지_기본설정시_예외() {
            // given
            UUID addressId = UUID.randomUUID();
            given(addressRepository.findByUserUserIdAndIsDefaultTrue(userId)).willReturn(Optional.empty());
            given(addressRepository.findById(addressId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> addressService.setDefaultAddress(userId, addressId))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("배송지 삭제")
    class deleteAddress {

        @Test
        void 본인_배송지_삭제_성공() {
            // given
            UUID addressId = UUID.randomUUID();

            given(mockUser.getUserId()).willReturn(userId);
            given(userAuthorizationService.getUserById(userId)).willReturn(mockUser);
            given(userAuthorizationService.isMaster(mockUser)).willReturn(false);

            Address address = Address.builder()
                    .user(mockUser)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when
            addressService.deleteAddress(userId, addressId);

            // then
            verify(addressRepository).findById(addressId);
        }

        @Test
        void MASTER_타인_배송지_삭제_성공() {
            // given
            UUID addressId = UUID.randomUUID();
            UUID masterId = UUID.randomUUID();
            UUID ownerUserId = UUID.randomUUID();

            User master = mock(User.class);
            User owner = mock(User.class);
            given(owner.getUserId()).willReturn(ownerUserId);
            given(userAuthorizationService.getUserById(masterId)).willReturn(master);
            given(userAuthorizationService.isMaster(master)).willReturn(true);

            Address address = Address.builder()
                    .user(owner)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when
            addressService.deleteAddress(masterId, addressId);

            // then
            verify(addressRepository).findById(addressId);
        }

        @Test
        void 존재하지_않는_배송지_삭제시_예외() {
            // given
            UUID addressId = UUID.randomUUID();
            given(addressRepository.findById(addressId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> addressService.deleteAddress(userId, addressId))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 타인_배송지_삭제시_예외() {
            // given
            UUID addressId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            User otherUser = mock(User.class);
            given(mockUser.getUserId()).willReturn(userId);
            given(userAuthorizationService.getUserById(otherUserId)).willReturn(otherUser);
            given(userAuthorizationService.isMaster(otherUser)).willReturn(false);

            Address address = Address.builder()
                    .user(mockUser)
                    .alias("집")
                    .address("서울시 강남구")
                    .detail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build();

            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when & then
            assertThatThrownBy(() -> addressService.deleteAddress(otherUserId, addressId))
                    .isInstanceOf(BaseException.class);
        }
    }

}