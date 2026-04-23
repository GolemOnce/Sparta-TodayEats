package com.sparta.todayeats.area.application.service;

import com.sparta.todayeats.area.domain.entity.Area;
import com.sparta.todayeats.area.domain.repository.AreaRepository;
import com.sparta.todayeats.area.presentation.dto.AreaCreateRequest;
import com.sparta.todayeats.area.presentation.dto.AreaCreateResponse;
import com.sparta.todayeats.area.presentation.dto.AreaResponse;
import com.sparta.todayeats.area.presentation.dto.AreaUpdateRequest;
import com.sparta.todayeats.category.presentation.dto.PageResponse;
import com.sparta.todayeats.global.exception.AreaErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


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


    // 운영 지역 목록 조회 && 검색
    public PageResponse<AreaResponse> getAreas(String keyword, Pageable pageable) {

        // keyword가 없으면 전체 조회, 있으면 이름 기준 검색
        Page<Area> result = findAreas(keyword, pageable);

        // 엔티티 → DTO 변환
        List<AreaResponse> content = result.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        // 페이지 응답 DTO 생성
        return PageResponse.<AreaResponse>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .sort(pageable.getSort().toString())
                .build();
    }


    // 운영 지역 상세 조회
    public AreaResponse getArea(UUID areaId) {
        Area area = getAreaEntity(areaId);
        return toResponse(area);
    }

    // 운영 지역 수정
    @Transactional
    public AreaResponse updateArea(UUID areaId, AreaUpdateRequest request) {

        // 수정 대상 운영 지역 조회
        Area area = getAreaEntity(areaId);

        // null이면 기존 값 유지
        String name = request.getName() != null ? normalizeName(request.getName()) : area.getName();
        String city = request.getCity() != null ? request.getCity() : area.getCity();
        String district = request.getDistrict() != null ? request.getDistrict() : area.getDistrict();
        Boolean isActive = request.getIsActive() != null ? request.getIsActive() : area.getIsActive();

        // 기존 이름과 다른 경우에만 중복 검증 수행
        if (!area.getName().equals(name)) {
            validateDuplicateArea(name);
        }

        // 운영 지역 이름 수정
        area.update(name, city, district, isActive);

        return toResponse(area);
    }





    // 운영지역 목록 조회 및 검색 처리
    private Page<Area> findAreas(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return areaRepository.findAll(pageable);
        }
        return areaRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    // 운영 지역 이름 기준 중복 조회 (대소문자 상관없이 존재 여부 확인)
    private void validateDuplicateArea(String name) {
        if (areaRepository.existsByNameIgnoreCase(name)) {
            throw new BaseException(AreaErrorCode.AREA_ALREADY_EXISTS);
        }
    }

    // Area 엔티티 → 목록 응답 DTO 변환
    private AreaResponse toResponse(Area area) {
        return AreaResponse.builder()
                .areaId(area.getId())
                .name(area.getName())
                .city(area.getCity())
                .district(area.getDistrict())
                .isActive(area.getIsActive())
                .createdAt(area.getCreatedAt())
                .createdBy(area.getCreatedBy())
                .updatedAt(area.getUpdatedAt())
                .updatedBy(area.getUpdatedBy())
                .build();
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

    // 운영 지역 엔티티 조회
    private Area getAreaEntity(UUID areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() -> new BaseException(AreaErrorCode.AREA_NOT_FOUND));
    }

}
