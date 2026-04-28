package com.sparta.todayeats.address.service;

import com.sparta.todayeats.address.dto.request.AddressCreateRequest;
import com.sparta.todayeats.address.dto.request.AddressUpdateRequest;
import com.sparta.todayeats.address.dto.response.*;
import com.sparta.todayeats.address.entity.Address;
import com.sparta.todayeats.address.repository.AddressRepository;
import com.sparta.todayeats.global.exception.*;
import com.sparta.todayeats.global.service.UserAuthorizationService;
import com.sparta.todayeats.user.entity.User;
import com.sparta.todayeats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;

    //
    private final UserRepository userRepository;
    private final UserAuthorizationService userAuthorizationService;

    // 배송지 등록
    @Transactional
    public AddressCreateResponse createAddress(UUID userId, AddressCreateRequest request) {
        // 1. userId로 User 조회
        User user = userRepository.findById(userId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        // 2. 배송지 생성
        Address address = Address.builder()
                .user(user)
                .alias(request.getAlias())
                .address(request.getAddress())
                .detail(request.getDetail())
                .zipCode(request.getZipCode())
                .isDefault(false)
                .build();

        address = addressRepository.save(address);

        return AddressCreateResponse.builder()
                .addressId(address.getId())
                .userId(userId)
                .alias(address.getAlias())
                .address(address.getAddress())
                .detail(address.getDetail())
                .zipCode(address.getZipCode())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .createdBy(address.getCreatedBy())
                .build();
    }

    // 배송지 조회
    @Transactional(readOnly = true)
    public AddressPageResponse getPagedAddresses(UUID userId, Pageable pageable) {
        // userId 유효성 검증
        if (!userRepository.existsById(userId)) {
            throw new BaseException(UserErrorCode.USER_NOT_FOUND);
        }

        Page<Address> addresses = addressRepository.findByUserId(userId, pageable);

        return AddressPageResponse.from(addresses);
    }

    // 배송지 상세 조회
    @Transactional(readOnly = true)
    public AddressDetailResponse getDetailAddress(UUID userId, UUID addressId) {
        // 1. 배송지 조회
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BaseException(AddressErrorCode.ADDRESS_NOT_FOUND));

        // 2. 권한 확인
        userAuthorizationService.validateSelf(userId, address.getUser().getUserId());

        return AddressDetailResponse.from(address);
    }

    // 배송지 수정
    @Transactional
    public AddressUpdateResponse updateAddress(UUID userId, UUID addressId, AddressUpdateRequest request) {
        // 1. 배송지 조회
        Address address =  addressRepository.findById(addressId)
                .orElseThrow(() -> new BaseException(AddressErrorCode.ADDRESS_NOT_FOUND));

        // 2. 권한 확인
        userAuthorizationService.validateSelf(userId, address.getUser().getUserId());

        address.updateAddress(request);

        return AddressUpdateResponse.from(address);
    }

    // 기본 배송지 설정
    @Transactional
    public AddressDefaultResponse setDefaultAddress(UUID userId, UUID addressId) {
        // 1. 기존 기본 배송지 false로
        addressRepository.findByUserUserIdAndIsDefaultTrue(userId)
                .ifPresent(address -> address.updateDefault(false));

        // 2. 새 기본 배송지 true로
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BaseException(AddressErrorCode.ADDRESS_NOT_FOUND));

        userAuthorizationService.validateSelf(userId, address.getUser().getUserId());

        address.updateDefault(true);

        return AddressDefaultResponse.from(address);
    }

    // 배송지 삭제
    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        // 1. 배송지 조회
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BaseException(AddressErrorCode.ADDRESS_NOT_FOUND));

        // 2. 권한 확인 (본인 또는 MASTER)
        boolean isSelf = address.getUser().getUserId().equals(userId);
        User user = userAuthorizationService.getUserById(userId);
        boolean isMaster = userAuthorizationService.isMaster(user);

        if (!isSelf && !isMaster) {
            throw new BaseException(AddressErrorCode.ADDRESS_ACCESS_DENIED);
        }

        // 3. 소프트 딜리트
        address.softDelete(userId);
    }
}
