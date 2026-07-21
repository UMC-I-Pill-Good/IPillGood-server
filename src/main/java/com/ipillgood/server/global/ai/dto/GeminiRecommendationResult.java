package com.ipillgood.server.global.ai.dto;

import java.util.List;

// Gemini 구조화 출력(responseSchema) 파싱 결과
public record GeminiRecommendationResult(
        String healthSummary,
        List<Item> recommendations
) {
    public record Item(
            Long ingredientId,
            String aiReason
    ) {
    }
}
