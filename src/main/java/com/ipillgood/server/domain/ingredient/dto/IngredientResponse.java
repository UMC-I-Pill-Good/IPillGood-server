package com.ipillgood.server.domain.ingredient.dto;

import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Map;

public class IngredientResponse {

    @Schema(description = "금기 조건 목록 조회 응답")
    @Builder
    public record ContraindicationList(
            @Schema(description = "조회 조건에 해당하는 금기 조건 전체 목록")
            List<ContraindicationItem> contraindications,

            @Schema(
                    description = "금기 조건 유형별 목록. MEDICATION, UNDERLYING_DISEASE, ALLERGY 키를 항상 포함합니다.",
                    example = """
                            {
                              "MEDICATION": [
                                {
                                  "contraindicationId": 1,
                                  "conditionName": "와파린"
                                }
                              ],
                              "UNDERLYING_DISEASE": [],
                              "ALLERGY": []
                            }
                            """
            )
            Map<String, List<GroupedContraindicationItem>> groupedByType
    ) {
    }

    @Schema(description = "금기 조건 항목")
    @Builder
    public record ContraindicationItem(
            @Schema(description = "금기 조건 ID", example = "1")
            Long contraindicationId,

            @Schema(description = "금기 조건 유형", example = "MEDICATION")
            ContraindicationType type,

            @Schema(description = "조건명", example = "와파린")
            String conditionName
    ) {
    }

    @Schema(description = "유형별 그룹에 포함되는 금기 조건 항목")
    @Builder
    public record GroupedContraindicationItem(
            @Schema(description = "금기 조건 ID", example = "1")
            Long contraindicationId,

            @Schema(description = "조건명", example = "와파린")
            String conditionName
    ) {
    }
}
