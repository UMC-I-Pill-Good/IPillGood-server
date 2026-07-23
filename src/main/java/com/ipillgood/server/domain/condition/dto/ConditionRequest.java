package com.ipillgood.server.domain.condition.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ConditionRequest {

    @Schema(description = "주간 컨디션 체크 저장 요청")
    public record SaveWeeklyRecord(
            @Schema(description = "활력 점수 (1~5)", example = "4")
            Integer vitalityScore,

            @Schema(description = "수면 시간 (0~23)", example = "7")
            Integer sleepHours,

            @Schema(description = "수면 분 (0~59)", example = "30")
            Integer sleepMinutes
    ) {
    }
}
