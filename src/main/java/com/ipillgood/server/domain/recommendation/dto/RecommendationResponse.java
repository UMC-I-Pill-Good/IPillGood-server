package com.ipillgood.server.domain.recommendation.dto;

import com.ipillgood.server.domain.recommendation.entity.enums.RecommendationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class RecommendationResponse {

    @Schema(description = "추천 생성 상태/결과 조회 응답")
    @Builder
    public record Detail(
            @Schema(description = "추천 ID", example = "1")
            Long recommendationId,

            @Schema(description = "추천 상태", example = "SUCCESS")
            RecommendationStatus status,

            @Schema(description = "건강 상태 요약", example = "피로와 수면 관리가 필요해 보여요.")
            String healthSummary,

            @Schema(description = "실패 사유", example = "null")
            String failureReason,

            @Schema(description = "추천 생성 시작 일시", example = "2026-07-20T13:00:00")
            LocalDateTime startedAt,

            @Schema(description = "추천 생성 완료 일시", example = "2026-07-20T13:01:00")
            LocalDateTime completedAt,

            @Schema(description = "추천 성분 항목")
            List<Item> items
    ) {
    }

    @Schema(description = "추천 성분 항목")
    @Builder
    public record Item(
            @Schema(description = "추천 항목 ID", example = "1")
            Long recommendationItemId,

            @Schema(description = "추천 순위", example = "1")
            Integer rankNo,

            @Schema(description = "성분 ID", example = "1")
            Long ingredientId,

            @Schema(description = "성분명", example = "마그네슘")
            String ingredientName,

            @Schema(description = "성분 이미지 URL", example = "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/magnesium.png")
            String imageUrl,

            @Schema(description = "효능 키워드 목록", example = "[\"긴장 완화\"]")
            List<String> effectKeywords,

            @Schema(description = "권장 섭취량", example = "1일 300mg")
            String recommendedIntake,

            @Schema(description = "권장 섭취 시간", example = "저녁 식후")
            String recommendedIntakeTime,

            @Schema(description = "추천 이유", example = "수면 질 관리에 도움")
            String aiReason
    ) {
    }

    @Schema(description = "추천 생성 재시도 응답")
    @Builder
    public record Retry(
            @Schema(description = "추천 ID", example = "1")
            Long recommendationId,

            @Schema(description = "재시도 후 상태", example = "PENDING")
            RecommendationStatus status,

            @Schema(description = "재시도 시작 일시", example = "2026-07-20T13:10:00")
            LocalDateTime startedAt
    ) {
    }

    @Schema(description = "추천 결과 확인 처리 응답")
    @Builder
    public record Confirm(
            @Schema(description = "확인 처리된 추천 ID", example = "1")
            Long recommendationId,

            @Schema(description = "온보딩 완료 여부", example = "true")
            boolean onboardingCompleted
    ) {
    }
}
