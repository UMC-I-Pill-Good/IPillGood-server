package com.ipillgood.server.domain.recommendation.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecommendationStatus {
    PENDING("대기"),
    SUCCESS("성공"),
    FAILED("실패"),
    NO_RESULT("결과 없음");

    private final String label;
}
