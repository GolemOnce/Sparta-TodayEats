package com.sparta.todayeats.area.application.service;

import com.sparta.todayeats.area.domain.entity.Area;
import com.sparta.todayeats.area.domain.repository.AreaRepository;
import com.sparta.todayeats.area.presentation.dto.AreaCreateRequest;
import com.sparta.todayeats.area.presentation.dto.AreaCreateResponse;
import com.sparta.todayeats.global.exception.AreaErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AreaService {

    private final AreaRepository areaRepository;

    // 운영 지역 생성
    @Transactional
    public AreaCreateResponse createArea(AreaCreateRequest request) {

        // 입력값 정규화
        String name = normalizeName(request.getName());

        // 중복 여부 검증 (city, district은 중복 가능)
        validateDuplicateArea(name);

        // 운영 지역 엔티티 생성
        Area area = Area.builder()
                .name(name)
                .city(request.getCity())
                .district(request.getDistrict())
                .isActive(true)
                .build();

        // DB 저장
        Area saved = areaRepository.save(area);

        // 응답 DTO로 변환
        return AreaCreateResponse.builder()
                .areaId(saved.getId())
                .name(saved.getName())
                .city(saved.getCity())
                .district(saved.getDistrict())
                .isActive(saved.getIsActive())
                .createdAt(saved.getCreatedAt())
                .createdBy(saved.getCreatedBy())
                .build();
    }

    // 운영 지역 이름 기준 중복 조회 (대소문자 상관없이 존재 여부 확인)
    private void validateDuplicateArea(String name) {
        if (areaRepository.existsByNameIgnoreCase(name)) {
            throw new BaseException(AreaErrorCode.AREA_ALREADY_EXISTS);
        }
    }

    // 운영지역 이름 정규화
    private String normalizeName(String name) {
        // null인 경우 예외 처리
        if (name == null) throw new BaseException(AreaErrorCode.INVALID_AREA_NAME);
        // 앞 뒤 공백 제거
        String normalized = name.trim();
        if (normalized.isBlank()) throw new BaseException(AreaErrorCode.INVALID_AREA_NAME);

        return normalized;
    }

}
