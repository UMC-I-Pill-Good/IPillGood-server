package com.ipillgood.server.domain.intake.repository;

public record ActiveProductRow(
        Long activeProductId,
        Long memberProductId,
        Long productId,
        String productName,
        Long ingredientCount,
        String singleIngredientImageKey
) {
}

