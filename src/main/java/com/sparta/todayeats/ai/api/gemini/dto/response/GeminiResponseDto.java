package com.sparta.todayeats.ai.api.gemini.dto.response;

import java.util.List;

public record GeminiResponseDto(
        List<Candidate> candidates
) {
    public record Candidate(
            Content content
    ) {
    }

    public record Content(
            List<Part> parts
    ) {
    }

    public record Part(
            String text
    ) {
    }
}