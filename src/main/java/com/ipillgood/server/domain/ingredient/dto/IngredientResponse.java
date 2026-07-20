package com.ipillgood.server.domain.ingredient.dto;

import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

public class IngredientResponse {

    @Builder
    public record ContraindicationList(
            List<ContraindicationItem> contraindications,
            Map<String, List<GroupedContraindicationItem>> groupedByType
    ) {
    }

    @Builder
    public record ContraindicationItem(
            Long contraindicationId,
            ContraindicationType type,
            String conditionName
    ) {
    }

    @Builder
    public record GroupedContraindicationItem(
            Long contraindicationId,
            String conditionName
    ) {
    }
}

