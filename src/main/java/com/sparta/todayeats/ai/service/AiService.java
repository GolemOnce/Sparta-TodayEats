package com.sparta.todayeats.ai.service;

import com.sparta.todayeats.ai.api.gemini.client.GeminiClient;
import com.sparta.todayeats.ai.dto.response.AiProductDescriptionResponse;
import com.sparta.todayeats.ai.entity.AiRequestLogEntity;
import com.sparta.todayeats.ai.repository.AiRequestLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.sparta.todayeats.global.exception.AiRequestLogErrorCode;
import com.sparta.todayeats.global.exception.BaseException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final String SUFFIX = " 답변을 최대한 간결하게 50자 이하로";
    private static final int MAX_DESCRIPTION_LENGTH = 50;

    private final GeminiClient geminiClient;
    private final AiRequestLogRepository aiRequestLogRepository;

    @Transactional
    public AiProductDescriptionResponse generateProductDescription(
            String prompt,
            UUID userId
    ) {
        String requestPrompt = prompt + SUFFIX;

        String response = geminiClient.generateContent(requestPrompt);

        // 1. null or 빈값 체크
                if (response == null || response.isBlank()) {
                    throw new BaseException(AiRequestLogErrorCode.AI_RESPONSE_EMPTY);
                }

        // 2. 공백 제거
                response = response.trim();

        // 3. 길이 제한
                if (response.length() > MAX_DESCRIPTION_LENGTH) {
                    response = response.substring(0, MAX_DESCRIPTION_LENGTH);
                }

        aiRequestLogRepository.save(
                new AiRequestLogEntity(prompt, requestPrompt, response, userId)
        );

        return new AiProductDescriptionResponse(response);
    }
}
