package com.sparta.todayeats.ai.service;

import com.sparta.todayeats.ai.api.gemini.client.GeminiClient;
import com.sparta.todayeats.ai.dto.response.AiProductDescriptionResponse;
import com.sparta.todayeats.ai.entity.AiRequestLogEntity;
import com.sparta.todayeats.ai.repository.AiRequestLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final String SUFFIX = " 답변을 최대한 간결하게 50자 이하로";

    private final GeminiClient geminiClient;
    private final AiRequestLogRepository aiRequestLogRepository;

    @Transactional
    public AiProductDescriptionResponse generateProductDescription(
            String prompt,
            UUID userId
    ) {
        String requestPrompt = prompt + SUFFIX;

        String response = geminiClient.generateContent(requestPrompt);

        aiRequestLogRepository.save(
                new AiRequestLogEntity(prompt, requestPrompt, response, userId)
        );

        return new AiProductDescriptionResponse(response);
    }
}
