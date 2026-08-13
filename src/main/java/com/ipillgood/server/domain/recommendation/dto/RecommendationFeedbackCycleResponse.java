package com.ipillgood.server.domain.recommendation.dto;

import com.ipillgood.server.domain.recommendation.entity.enums.RecommendationFeedbackResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

public class RecommendationFeedbackCycleResponse {

    @Schema(description = "추천 피드백 대상 조회 응답")
    @Builder
    public record Due(
            @Schema(description = "피드백 팝업 노출 대상 여부", example = "true")
            Boolean due,

            @Schema(description = "피드백 사이클 ID", example = "1")
            Long cycleId,

            @Schema(description = "추천 ID", example = "1")
            Long recommendationId,

            @Schema(description = "피드백 예정일", example = "2026-08-19")
            LocalDate cycleDueOn
    ) {
    }

    @Schema(description = "추천 피드백 응답 저장 응답")
    @Builder
    public record Respond(
            @Schema(description = "피드백 사이클 ID", example = "1")
            Long cycleId,

            @Schema(description = "저장된 응답", example = "HELPFUL")
            RecommendationFeedbackResponse responseType,

            @Schema(description = "응답 일시", example = "2026-08-19T09:00:00")
            LocalDateTime respondedAt,

            @Schema(description = "다음 피드백 예정일", example = "2026-09-18")
            LocalDate nextCycleDueOn
    ) {
    }
}
