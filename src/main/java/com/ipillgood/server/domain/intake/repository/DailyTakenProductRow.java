package com.ipillgood.server.domain.intake.repository;

import java.time.LocalDateTime;

public record DailyTakenProductRow(
        Long activeProductId,
        Long productId,
        String productName,
        LocalDateTime takenAt
) {
}
