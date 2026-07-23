package com.ipillgood.server.domain.cabinet.repository;

public record CabinetReviewPromptRow(
        Long activeProductId,
        Long productId,
        String productName
) {
}
