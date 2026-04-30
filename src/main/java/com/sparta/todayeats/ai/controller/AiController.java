package com.sparta.todayeats.ai.controller;

import com.sparta.todayeats.ai.dto.request.AiProductDescriptionRequest;
import com.sparta.todayeats.ai.dto.response.AiProductDescriptionResponse;
import com.sparta.todayeats.ai.service.AiService;
import com.sparta.todayeats.global.annotation.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.todayeats.global.response.ApiResponse;

import java.util.UUID;

@Tag(name = "AI")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI 상품 설명 생성")
    @PostMapping("/product-description")
    @PreAuthorize("hasRole('OWNER')") // 필수 기능 구현: 오너 역할로만 리퀘스트 가능
    public ResponseEntity<ApiResponse<AiProductDescriptionResponse>> generateProductDescription(
            @Valid @RequestBody AiProductDescriptionRequest request,
            @Parameter(hidden = true) @LoginUser UUID userId
    ) {
        AiProductDescriptionResponse response =
                aiService.generateProductDescription(request.prompt(), userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
