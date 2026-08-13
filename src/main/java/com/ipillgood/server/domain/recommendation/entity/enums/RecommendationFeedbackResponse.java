package com.ipillgood.server.domain.recommendation.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecommendationFeedbackResponse {
    HELPFUL("도움이 돼요"),
    UNSURE("잘 모르겠어요");

    private final String label;
}
