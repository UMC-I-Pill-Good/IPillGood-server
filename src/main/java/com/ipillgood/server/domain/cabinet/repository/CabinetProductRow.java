package com.ipillgood.server.domain.cabinet.repository;

import java.time.LocalDateTime;

public record CabinetProductRow(
        Long memberProductId,
        Long productId,
        String productName,
        LocalDateTime addedAt,
        Long activeProductId,
        Long ingredientCount,
        String singleIngredientImageKey
) {
}
