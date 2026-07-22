package com.ipillgood.server.domain.intake.dto;

import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class IntakeResponse {

    @Schema(description = "섭취 중 영양제 목록 조회 응답")
    @Builder
    public record ActiveProducts(
            @Schema(description = "현재 섭취 중인 영양제 수", example = "2")
            Integer totalCount,

            @Schema(description = "현재 섭취 중인 영양제 목록")
            List<ActiveProductSummary> activeProducts
    ) {
    }

    @Schema(description = "섭취 중 영양제 카드 항목")
    @Builder
    public record ActiveProductSummary(
            @Schema(description = "활성 섭취 중 상품 ID", example = "7")
            Long activeProductId,

            @Schema(description = "회원 캐비닛 상품 ID", example = "15")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "카드에 표시할 썸네일 이미지 URL")
            String thumbnailImageUrl
    ) {
    }

    @Schema(description = "오늘 복용 상태 조회 응답")
    @Builder
    public record TodayIntakeStatus(
            @Schema(description = "서비스 기준 오늘 날짜", example = "2026-07-21")
            LocalDate currentDate,

            @Schema(description = "오늘 복용 예정 영양제 수", example = "2")
            Integer scheduledCount,

            @Schema(description = "오늘 실제 섭취 완료한 영양제 수", example = "1")
            Integer takenCount,

            @Schema(description = "오늘 복용 예정 영양제를 모두 완료했는지 여부", example = "false")
            Boolean allCompleted,

            @Schema(description = "홈 미섭취 안내 박스 노출 여부", example = "true")
            Boolean missedNoticeVisible,

            @Schema(description = "오늘 자동 팝업 노출 기록 존재 여부", example = "false")
            Boolean autoPopupShown,

            @Schema(description = "홈 첫 진입 시 자동 팝업을 노출해야 하는지 여부", example = "true")
            Boolean autoPopupRequired,

            @Schema(description = "오늘 복용 예정 영양제 목록")
            List<TodayScheduledProduct> scheduledProducts
    ) {
    }

    @Schema(description = "오늘 복용 예정 영양제 항목")
    @Builder
    public record TodayScheduledProduct(
            @Schema(description = "활성 섭취 중 상품 ID", example = "7")
            Long activeProductId,

            @Schema(description = "회원 캐비닛 상품 ID", example = "15")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "오늘 섭취 완료 여부", example = "true")
            Boolean taken,

            @Schema(description = "오늘 섭취 완료 일시", example = "2026-07-21T08:45:00")
            LocalDateTime takenAt
    ) {
    }

    @Schema(description = "오늘 복용 팝업 노출 기록 응답")
    @Builder
    public record TodayPopupShown(
            @Schema(description = "서비스 기준 오늘 날짜", example = "2026-07-21")
            LocalDate currentDate,

            @Schema(description = "오늘 자동 팝업 노출 기록 존재 여부", example = "true")
            Boolean autoPopupShown,

            @Schema(description = "자동 팝업 노출 기록 일시", example = "2026-07-21T09:00:00")
            LocalDateTime autoPopupShownAt
    ) {
    }

    @Schema(description = "오늘 복용 체크 저장 응답")
    @Builder
    public record SaveTodayIntakeRecords(
            @Schema(description = "서비스 기준 오늘 날짜", example = "2026-07-21")
            LocalDate currentDate,

            @Schema(description = "오늘 복용 예정 영양제 수", example = "2")
            Integer scheduledCount,

            @Schema(description = "오늘 실제 섭취 완료한 영양제 수", example = "1")
            Integer takenCount,

            @Schema(description = "오늘 복용 예정 영양제를 모두 완료했는지 여부", example = "false")
            Boolean allCompleted,

            @Schema(description = "전체 완료 일시", example = "2026-07-21T21:05:00")
            LocalDateTime completedAt,

            @Schema(description = "홈 미섭취 안내 박스 노출 여부", example = "true")
            Boolean missedNoticeVisible,

            @Schema(description = "오늘 복용 예정 영양제별 저장 결과")
            List<TodayIntakeRecord> records
    ) {
    }

    @Schema(description = "오늘 복용 체크 저장 항목")
    @Builder
    public record TodayIntakeRecord(
            @Schema(description = "활성 섭취 중 상품 ID", example = "7")
            Long activeProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "오늘 복용 예정 여부", example = "true")
            Boolean scheduled,

            @Schema(description = "오늘 섭취 완료 여부", example = "true")
            Boolean taken,

            @Schema(description = "오늘 섭취 완료 일시", example = "2026-07-21T08:45:00")
            LocalDateTime takenAt
    ) {
    }

    @Schema(description = "섭취 중 영양제 등록 응답")
    @Builder
    public record RegisterActiveProduct(
            @Schema(description = "생성된 활성 섭취 중 상품 ID", example = "8")
            Long activeProductId,

            @Schema(description = "회원 캐비닛 상품 ID", example = "16")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "124")
            Long productId,

            @Schema(description = "영양제 상품명", example = "헬로바이오 맥스 비타민C 3000")
            String productName,

            @Schema(description = "카드에 표시할 썸네일 이미지 URL")
            String thumbnailImageUrl,

            @Schema(description = "개별 복용 알림 ON/OFF 여부", example = "true")
            Boolean notificationEnabled,

            @Schema(description = "복용 시간", example = "08:30")
            String intakeTime,

            @Schema(description = "복용 주기 enum", example = "EVERY_DAY")
            String frequency,

            @Schema(description = "화면에 표시할 복용 주기명", example = "매일")
            String frequencyLabel
    ) {
    }

    @Schema(description = "섭취 중 영양제 설정 변경 응답")
    @Builder
    public record UpdateActiveProductSettings(
            @Schema(description = "활성 섭취 중 상품 ID", example = "7")
            Long activeProductId,

            @Schema(description = "회원 캐비닛 상품 ID", example = "15")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "브랜드명", example = "뉴트리코어")
            String brand,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "카드에 표시할 썸네일 이미지 URL")
            String thumbnailImageUrl,

            @Schema(description = "섭취 중으로 추가한 날짜", example = "2026-07-01")
            LocalDate startedOn,

            @Schema(description = "N일째 섭취 중 배지에 사용할 섭취 일수", example = "21")
            Integer intakeDayCount,

            @Schema(description = "개별 복용 알림 ON/OFF 여부", example = "false")
            Boolean notificationEnabled,

            @Schema(description = "복용 시간", example = "21:00")
            String intakeTime,

            @Schema(description = "복용 주기 enum", example = "EVERY_2_DAYS")
            String frequency,

            @Schema(description = "화면에 표시할 복용 주기명", example = "2일에 한 번")
            String frequencyLabel,

            @Schema(description = "복용 주기 간격 일수", example = "2")
            Integer frequencyIntervalDays,

            @Schema(description = "복용 예정일 계산 기준일", example = "2026-07-21")
            LocalDate scheduleAnchorOn
    ) {
    }

    @Schema(description = "섭취 중 영양제 제거 응답")
    @Builder
    public record RemoveActiveProduct(
            @Schema(description = "중단 처리된 활성 섭취 중 상품 ID", example = "7")
            Long activeProductId,

            @Schema(description = "회원 캐비닛 상품 ID", example = "15")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "섭취 중단일", example = "2026-07-21")
            LocalDate stoppedOn
    ) {
    }

    @Schema(description = "섭취 중 등록 전 병용 금기 확인 응답")
    @Builder
    public record CompatibilityCheck(
            @Schema(description = "병용 금기 또는 주의 조합 존재 여부", example = "true")
            Boolean hasConflicts,

            @Schema(description = "감지된 병용 금기 또는 주의 조합 목록")
            List<CompatibilityConflict> conflicts
    ) {
    }

    @Schema(description = "병용 금기 또는 주의 조합 항목")
    @Builder
    public record CompatibilityConflict(
            @Schema(description = "조합 유형", example = "CAUTION")
            CombinationType combinationType,

            @Schema(description = "현재 섭취 중 영양제의 매칭 성분 ID", example = "10")
            Long currentIngredientId,

            @Schema(description = "현재 섭취 중 영양제의 매칭 성분명", example = "칼슘")
            String currentIngredientName,

            @Schema(description = "새로 등록하려는 영양제의 매칭 성분 ID", example = "18")
            Long targetIngredientId,

            @Schema(description = "새로 등록하려는 영양제의 매칭 성분명", example = "철")
            String targetIngredientName,

            @Schema(description = "함께 복용할 때 권장되지 않거나 주의가 필요한 이유")
            String reason
    ) {
    }
}
