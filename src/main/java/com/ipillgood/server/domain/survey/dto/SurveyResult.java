package com.ipillgood.server.domain.survey.dto;

import com.ipillgood.server.domain.recommendation.entity.enums.RecommendationStatus;
import java.time.LocalDateTime;
import lombok.Builder;

public class SurveyResult {

    // 설문 응답 저장 및 추천 생성 시작 응답
    @Builder
    public record Submit(
            Long surveyResponseId,
            Long recommendationId,
            RecommendationStatus recommendationStatus,
            LocalDateTime completedAt
    ) {
    }
}
