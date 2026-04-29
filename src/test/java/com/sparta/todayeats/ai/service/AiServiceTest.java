package com.sparta.todayeats.ai.service;

import com.sparta.todayeats.ai.api.gemini.client.GeminiClient;
import com.sparta.todayeats.ai.dto.response.AiProductDescriptionResponse;
import com.sparta.todayeats.ai.entity.AiRequestLogEntity;
import com.sparta.todayeats.ai.repository.AiRequestLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {
    @Mock
    private GeminiClient geminiClient;

    @Mock
    private AiRequestLogRepository aiRequestLogRepository;

    @InjectMocks
    private AiService aiService;

    @Test
    void AI_상품_설명_생성_성공() {
        // given
        UUID userId = UUID.randomUUID();
        String prompt = "만두 상품의 이름을 추천해줘";
        String aiResponse = "육즙 가득 만두";

        given(geminiClient.generateContent(prompt + " 답변을 최대한 간결하게 50자 이하로"))
                .willReturn(aiResponse);

        // when
        AiProductDescriptionResponse response =
                aiService.generateProductDescription(prompt, userId);

        // then
        assertThat(response.description()).isEqualTo(aiResponse);

        verify(geminiClient).generateContent(prompt + " 답변을 최대한 간결하게 50자 이하로");
        verify(aiRequestLogRepository).save(any(AiRequestLogEntity.class));
    }
}
