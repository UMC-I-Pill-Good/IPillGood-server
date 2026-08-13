package com.ipillgood.server.domain.cabinet.repository;

import java.time.LocalDateTime;

public record CabinetProductRow(
        Long memberProductId,
        Long productId,
        String productName,
        Boolean mfdsCertified,
        LocalDateTime addedAt,
        Long activeProductId,
        Long ingredientCount,
        String singleIngredientImageKey
) {
}
