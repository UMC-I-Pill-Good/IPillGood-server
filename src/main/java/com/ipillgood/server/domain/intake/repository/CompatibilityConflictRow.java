package com.ipillgood.server.domain.intake.repository;

import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;

public record CompatibilityConflictRow(
        Long combinationId,
        CombinationType combinationType,
        Long currentIngredientId,
        String currentIngredientName,
        Long targetIngredientId,
        String targetIngredientName,
        String reason
) {
}

