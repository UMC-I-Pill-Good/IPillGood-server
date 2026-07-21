package com.ipillgood.server.domain.recommendation.dto;

import com.ipillgood.server.domain.recommendation.entity.enums.RecommendationFeedbackResponse;
import io.swagger.v3.oas.annotations.media.Schema;

public class RecommendationFeedbackCycleRequest {

    @Schema(description = "추천 피드백 응답 저장 요청")
    public record Respond(
            @Schema(description = "피드백 응답", example = "HELPFUL")
            RecommendationFeedbackResponse responseType
    ) {
    }
}
