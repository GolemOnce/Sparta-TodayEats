package com.sparta.todayeats.ai.api.gemini.client;

import com.sparta.todayeats.ai.api.gemini.dto.response.GeminiResponseDto;
import com.sparta.todayeats.global.exception.AiRequestLogErrorCode;
import com.sparta.todayeats.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public String generateContent(String prompt) {
        String url = apiUrl + "?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        ResponseEntity<GeminiResponseDto> response;

        try {
            response = restTemplate.postForEntity(url, body, GeminiResponseDto.class);
        } catch (ResourceAccessException e) {
            throw new BaseException(AiRequestLogErrorCode.AI_TIMEOUT); // AI_TIMEOUT
        } catch (Exception e) {
            throw new BaseException(AiRequestLogErrorCode.AI_API_ERROR); // 네트워크 오류, 키 오류, 서버 오류
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BaseException(AiRequestLogErrorCode.AI_API_ERROR); // 400, 500
        }

        GeminiResponseDto bodyResponse = response.getBody();

        if (bodyResponse == null
                || bodyResponse.candidates() == null
                || bodyResponse.candidates().isEmpty()
                || bodyResponse.candidates().get(0).content() == null
                || bodyResponse.candidates().get(0).content().parts() == null
                || bodyResponse.candidates().get(0).content().parts().isEmpty()
                || bodyResponse.candidates().get(0).content().parts().get(0).text() == null
                || bodyResponse.candidates().get(0).content().parts().get(0).text().isBlank()) {
            throw new BaseException(AiRequestLogErrorCode.AI_RESPONSE_EMPTY); // AI_RESPONSE_EMPTY
        }

        return bodyResponse.candidates()
                .get(0)
                .content()
                .parts()
                .get(0)
                .text();
    }
}
