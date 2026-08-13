package com.ipillgood.server.domain.cabinet.repository;

public record CabinetProductIngredientKeywordRow(
        Long ingredientId,
        String name,
        String imageKey,
        String description,
        String effectKeyword
) {
}
