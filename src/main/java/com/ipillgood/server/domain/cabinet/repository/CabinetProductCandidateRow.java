package com.ipillgood.server.domain.cabinet.repository;

public record CabinetProductCandidateRow(
        Long productId,
        String brand,
        String productName,
        Long ingredientCount,
        String singleIngredientImageKey,
        Double averageRating,
        Long reviewCount,
        Boolean isOwned
) {
}
