package com.sparta.todayeats.area.presentation.controller;

import com.sparta.todayeats.area.application.service.AreaService;
import com.sparta.todayeats.area.domain.entity.Area;
import com.sparta.todayeats.area.presentation.dto.AreaCreateRequest;
import com.sparta.todayeats.area.presentation.dto.AreaCreateResponse;
import com.sparta.todayeats.area.presentation.dto.AreaResponse;
import com.sparta.todayeats.category.presentation.dto.PageResponse;
import com.sparta.todayeats.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    // TODO: 권한 처리(MANAGER, MASTER), Auditing
    // 운영 지역 생성
    @PostMapping
    public ResponseEntity<ApiResponse<AreaCreateResponse>> createArea(@Valid @RequestBody AreaCreateRequest request) {
        AreaCreateResponse response = areaService.createArea(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    // TODO: Auditing
    // 운영 지역 목록 조회 + 검색
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AreaResponse>>> getAreas(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        PageResponse<AreaResponse> response = areaService.getAreas(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // TODO: Auditing
    // 운영 지역 상세 조회
    @GetMapping("/{areaId}")
    public ResponseEntity<ApiResponse<AreaResponse>> getArea(@PathVariable UUID areaId) {

        AreaResponse response = areaService.getArea(areaId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }



}
