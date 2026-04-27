package com.sparta.todayeats.ai.controller;

import com.sparta.todayeats.ai.dto.request.AiProductDescriptionRequest;
import com.sparta.todayeats.ai.dto.response.AiProductDescriptionResponse;
import com.sparta.todayeats.ai.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/product-description")
    @PreAuthorize("hasRole('OWNER')") // 필수 기능 구현: 오너 역할로만 리퀘스트 가능
    public ResponseEntity<AiProductDescriptionResponse> generateProductDescription(
            @Valid @RequestBody AiProductDescriptionRequest request,
            @AuthenticationPrincipal UUID userId
    ) {
        return ResponseEntity.ok(
                aiService.generateProductDescription(request.prompt(), userId)
        );
    }
}
