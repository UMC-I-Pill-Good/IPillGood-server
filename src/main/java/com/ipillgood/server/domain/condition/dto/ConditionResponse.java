package com.ipillgood.server.domain.condition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class ConditionResponse {

    @Schema(description = "이번 주 컨디션 체크 상태 조회 응답")
    @Builder
    public record CurrentWeek(
            @Schema(description = "서버 기준 오늘 날짜", example = "2026-07-26")
            LocalDate today,

            @Schema(description = "이번 주 시작일", example = "2026-07-20")
            LocalDate weekStartOn,

            @Schema(description = "이번 주 종료일", example = "2026-07-26")
            LocalDate weekEndOn,

            @Schema(description = "오늘이 일요일인지 여부", example = "true")
            Boolean isSunday,

            @Schema(description = "체크 가능 여부", example = "true")
            Boolean checkAvailable,

            @Schema(description = "체크 완료 여부", example = "false")
            Boolean checked,

            @Schema(description = "주간 컨디션 기록 ID", example = "1")
            Long recordId,

            @Schema(description = "자동 팝업 노출 가능 여부", example = "true")
            Boolean autoPopupAvailable,

            @Schema(description = "자동 팝업 노출 일시", example = "null")
            LocalDateTime autoShownAt,

            @Schema(description = "팝업 닫힘 일시", example = "null")
            LocalDateTime dismissedAt,

            @Schema(description = "일요일 영양제 미섭취 확인 팝업 필요 여부", example = "true")
            Boolean sundayIntakeWarningRequired
    ) {
    }

    @Schema(description = "주간 컨디션 기록 상세 응답")
    @Builder
    public record Detail(
            @Schema(description = "주간 컨디션 기록 ID", example = "1")
            Long recordId,

            @Schema(description = "주 시작일", example = "2026-07-20")
            LocalDate weekStartOn,

            @Schema(description = "주 종료일", example = "2026-07-26")
            LocalDate weekEndOn,

            @Schema(description = "체크일", example = "2026-07-26")
            LocalDate checkedOn,

            @Schema(description = "활력 점수", example = "4")
            Integer vitalityScore,

            @Schema(description = "수면 시간", example = "7")
            Integer sleepHours,

            @Schema(description = "수면 분", example = "30")
            Integer sleepMinutes,

            @Schema(description = "수면 점수", example = "5")
            Integer sleepScore,

            @Schema(description = "복용 일수", example = "6")
            Integer intakeDaysCount,

            @Schema(description = "복용 점수", example = "5")
            Integer intakeScore,

            @Schema(description = "컨디션 점수", example = "4.67")
            BigDecimal conditionScore
    ) {
    }

    @Schema(description = "월별 컨디션 그래프 조회 응답")
    @Builder
    public record MonthlySummary(
            @Schema(description = "조회 연도", example = "2026")
            Integer year,

            @Schema(description = "조회 월", example = "7")
            Integer month,

            @Schema(description = "월 평균 컨디션 점수", example = "4.2")
            BigDecimal averageConditionScore,

            @Schema(description = "월 평균 활력 점수", example = "4.1")
            BigDecimal averageVitalityScore,

            @Schema(description = "월 평균 수면 시간", example = "7.3")
            BigDecimal averageSleepHours,

            @Schema(description = "월 평균 주간 섭취 기록 일수", example = "6.5")
            BigDecimal averageIntakeDaysCount,

            @Schema(description = "주차별 기록 목록")
            List<WeeklySummary> records
    ) {
    }

    @Schema(description = "컨디션 팝업 자동 노출 기록 응답")
    @Builder
    public record PopupAutoShown(
            @Schema(description = "컨디션 팝업 로그 ID", example = "1")
            Long popupLogId,

            @Schema(description = "주 시작일", example = "2026-07-20")
            LocalDate weekStartOn,

            @Schema(description = "자동 노출 일시", example = "2026-07-26T09:00:00")
            LocalDateTime autoShownAt
    ) {
    }

    @Schema(description = "컨디션 팝업 닫힘 기록 응답")
    @Builder
    public record PopupDismissed(
            @Schema(description = "컨디션 팝업 로그 ID", example = "1")
            Long popupLogId,

            @Schema(description = "주 시작일", example = "2026-07-20")
            LocalDate weekStartOn,

            @Schema(description = "닫힘 일시", example = "2026-07-26T09:05:00")
            LocalDateTime dismissedAt
    ) {
    }

    @Schema(description = "주차별 컨디션 요약")
    @Builder
    public record WeeklySummary(
            @Schema(description = "주간 기록 ID", example = "1")
            Long recordId,

            @Schema(description = "주 시작일", example = "2026-07-20")
            LocalDate weekStartOn,

            @Schema(description = "주 종료일", example = "2026-07-26")
            LocalDate weekEndOn,

            @Schema(description = "컨디션 점수", example = "4.67")
            BigDecimal conditionScore
    ) {
    }
}
