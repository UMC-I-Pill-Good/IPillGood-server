package com.ipillgood.server.domain.intake.dto;

import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

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
