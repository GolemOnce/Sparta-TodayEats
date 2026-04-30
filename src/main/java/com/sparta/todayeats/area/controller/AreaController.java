package com.sparta.todayeats.area.controller;

import com.sparta.todayeats.area.service.AreaService;
import com.sparta.todayeats.area.dto.request.AreaCreateRequest;
import com.sparta.todayeats.area.dto.response.AreaCreateResponse;
import com.sparta.todayeats.area.dto.response.AreaResponse;
import com.sparta.todayeats.area.dto.request.AreaUpdateRequest;
import com.sparta.todayeats.global.annotation.ApiNoContent;
import com.sparta.todayeats.global.annotation.ApiPageable;
import com.sparta.todayeats.global.annotation.LoginUser;
import com.sparta.todayeats.global.response.PageResponse;
import com.sparta.todayeats.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Area")
@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @Operation(summary = "지역 등록")
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    public ResponseEntity<ApiResponse<AreaCreateResponse>> createArea(@Valid @RequestBody AreaCreateRequest request) {
        AreaCreateResponse response = areaService.createArea(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @Operation(summary = "지역 목록 조회")
    @ApiPageable
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AreaResponse>>> getAreas(
            @Parameter(description = "가게 이름", example = "맛있는 한식당")
            @RequestParam(required = false) String keyword,
            @Parameter(hidden = true)
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        PageResponse<AreaResponse> response = areaService.getAreas(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "지역 상세 조회")
    @GetMapping("/{areaId}")
    public ResponseEntity<ApiResponse<AreaResponse>> getArea(
            @Parameter(description = "지역 ID", example = "770e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID areaId
    ) {

        AreaResponse response = areaService.getArea(areaId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "지역 수정")
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    @PutMapping("/{areaId}")
    public ResponseEntity<ApiResponse<AreaResponse>> updateArea(
            @Parameter(description = "지역 ID", example = "770e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID areaId,
            @Valid @RequestBody AreaUpdateRequest request
    ) {

        AreaResponse response = areaService.updateArea(areaId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "지역 삭제")
    @ApiNoContent
    @PreAuthorize("hasAnyRole('MASTER')")
    @DeleteMapping("/{areaId}")
    public ResponseEntity<Void> deleteArea(
            @Parameter(description = "지역 ID", example = "770e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID areaId,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {

        areaService.deleteArea(areaId,userId);

        return ResponseEntity.noContent().build();
    }



}
