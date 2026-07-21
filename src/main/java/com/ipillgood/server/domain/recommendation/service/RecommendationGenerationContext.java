package com.ipillgood.server.domain.recommendation.service;

import java.util.List;

// Gemini 호출에 필요한 프롬프트와 2차 검증(환각 방지)에 사용할 후보 성분 ID 목록
public record RecommendationGenerationContext(String prompt, List<Long> candidateIngredientIds) {

    public static RecommendationGenerationContext empty() {
        return new RecommendationGenerationContext(null, List.of());
    }
}
