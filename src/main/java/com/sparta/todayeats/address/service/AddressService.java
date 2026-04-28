package com.sparta.todayeats.address.service;

import com.sparta.todayeats.address.dto.reqeust.AddressCreateRequest;
import com.sparta.todayeats.address.dto.response.AddressCreateResponse;
import com.sparta.todayeats.address.dto.response.AddressPageResponse;
import com.sparta.todayeats.address.entity.Address;
import com.sparta.todayeats.address.repository.AddressRepository;
import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.UserErrorCode;
import com.sparta.todayeats.user.domain.entity.User;
import com.sparta.todayeats.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;

    //
    private final UserRepository userRepository;

    // 배송지 등록
    @Transactional
    public AddressCreateResponse createAddress(UUID userId, AddressCreateRequest request) {
        // 1. userId로 User 조회
        User user = userRepository.findById(userId)
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

    // 배송지 수정

    // 기본 배송지 설정

    // 배송지 삭제
}
