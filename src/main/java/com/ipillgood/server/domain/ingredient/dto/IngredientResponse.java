package com.ipillgood.server.domain.ingredient.dto;

import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class IngredientResponse {

    @Schema(description = "영양성분 목록 조회 응답")
    @Builder
    public record IngredientList(
            @Schema(description = "영양성분 목록")
            List<IngredientSummary> ingredients
    ) {
    }

    @Schema(description = "영양성분 요약 항목")
    @Builder
    public record IngredientSummary(
            @Schema(description = "영양성분 ID", example = "2")
            Long ingredientId,

            @Schema(description = "성분명", example = "비타민 D")
            String name,

            @Schema(
                    description = "성분 이미지 URL",
                    example = "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png"
            )
            String imageUrl
    ) {
    }

    @Schema(description = "영양성분 상세 조회 응답")
    @Builder
    public record IngredientDetail(
            @Schema(description = "영양성분 ID", example = "2")
            Long ingredientId,

            @Schema(description = "성분명", example = "비타민 D")
            String name,

            @Schema(description = "성분 설명")
            String description,

            @Schema(description = "성분 이미지 URL")
            String imageUrl,

            @Schema(description = "효능 문구 목록")
            List<String> effects,

            @Schema(description = "부작용 및 주의사항 문구 목록")
            List<String> cautions,

            @Schema(description = "함께 섭취 시 주의가 필요한 성분 조합 목록")
            List<ContraindicatedCombination> contraindicatedCombinations,

            @Schema(description = "권장 섭취량", nullable = true)
            String recommendedIntake,

            @Schema(description = "권장 섭취 시간대", nullable = true)
            String recommendedIntakeTime,

            @Schema(description = "사용자의 캐비닛에 현재 성분을 포함한 영양제가 있는지 여부")
            Boolean hasCabinetProduct,

            @Schema(description = "사용자의 섭취 중인 영양제에 현재 성분을 포함한 영양제가 있는지 여부")
            Boolean hasIntakeProduct,

            @Schema(description = "대체 음식 목록")
            List<AlternativeFoodItem> alternativeFoods
    ) {
    }

    @Schema(description = "병용 금기 조합 항목")
    @Builder
    public record ContraindicatedCombination(
            @Schema(description = "현재 성분과 함께 섭취 시 주의가 필요한 상대 성분 ID", example = "10")
            Long targetIngredientId,

            @Schema(description = "현재 성분과 함께 섭취 시 주의가 필요한 상대 성분명", example = "칼슘")
            String targetIngredientName,

            @Schema(description = "조합 유형", example = "CAUTION")
            CombinationType type,

            @Schema(description = "함께 섭취 시 주의가 필요한 이유", nullable = true)
            String reason
    ) {
    }

    @Schema(description = "대체 음식 항목")
    @Builder
    public record AlternativeFoodItem(
            @Schema(description = "음식 이름", example = "연어")
            String name,

            @Schema(description = "100g당 해당 성분 함량", example = "100g당 비타민 D 10μg")
            String contentPer100g
    ) {
    }

    @Schema(description = "금기 조건 목록 조회 응답")
    @Builder
    public record ContraindicationList(
            @Schema(description = "설문 선택 영역별 금기 조건 그룹 목록")
            List<ContraindicationGroup> groups
    ) {
    }

    @Schema(description = "금기 조건 그룹")
    @Builder
    public record ContraindicationGroup(
            @Schema(description = "금기 조건 유형", example = "UNDERLYING_DISEASE")
            ContraindicationType type,

            @Schema(description = "프론트에 표시할 설문 영역명", example = "기저질환")
            String label,

            @Schema(description = "해당 유형의 금기 조건 목록")
            List<ContraindicationItem> items
    ) {
    }

    @Schema(description = "금기 조건 항목")
    @Builder
    public record ContraindicationItem(
            @Schema(description = "금기 조건 ID", example = "23")
            Long contraindicationId,

            @Schema(description = "조건명", example = "고칼슘혈증")
            String conditionName
    ) {
    }
}
