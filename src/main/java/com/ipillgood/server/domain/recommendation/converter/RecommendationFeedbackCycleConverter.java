package com.ipillgood.server.domain.recommendation.converter;

import com.ipillgood.server.domain.recommendation.dto.RecommendationFeedbackCycleResponse;
import com.ipillgood.server.domain.recommendation.entity.RecommendationFeedbackCycle;

public class RecommendationFeedbackCycleConverter {

    public static RecommendationFeedbackCycleResponse.Due toDue(RecommendationFeedbackCycle cycle) {
        return RecommendationFeedbackCycleResponse.Due.builder()
                .due(true)
                .cycleId(cycle.getId())
                .recommendationId(cycle.getRecommendation().getId())
                .cycleDueOn(cycle.getCycleDueOn())
                .build();
    }

    public static RecommendationFeedbackCycleResponse.Due toNotDue() {
        return RecommendationFeedbackCycleResponse.Due.builder()
                .due(false)
                .build();
    }

    public static RecommendationFeedbackCycleResponse.Respond toRespond(RecommendationFeedbackCycle cycle) {
        return RecommendationFeedbackCycleResponse.Respond.builder()
                .cycleId(cycle.getId())
                .responseType(cycle.getResponseType())
                .respondedAt(cycle.getRespondedAt())
                .nextCycleDueOn(cycle.getNextCycleDueOn())
                .build();
    }
}
