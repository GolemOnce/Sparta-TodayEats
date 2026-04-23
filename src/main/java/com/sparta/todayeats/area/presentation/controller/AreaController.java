package com.sparta.todayeats.area.presentation.controller;

import com.sparta.todayeats.area.application.service.AreaService;
import com.sparta.todayeats.area.presentation.dto.AreaCreateRequest;
import com.sparta.todayeats.area.presentation.dto.AreaCreateResponse;
import com.sparta.todayeats.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
