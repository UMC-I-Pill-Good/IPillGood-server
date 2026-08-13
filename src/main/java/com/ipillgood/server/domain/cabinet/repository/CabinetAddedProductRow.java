package com.ipillgood.server.domain.cabinet.repository;

import java.time.LocalDateTime;

public record CabinetAddedProductRow(
        Long memberProductId,
        Long productId,
        String brand,
        String productName,
        LocalDateTime addedAt,
        Long ingredientCount,
        String singleIngredientImageKey
) {
}
