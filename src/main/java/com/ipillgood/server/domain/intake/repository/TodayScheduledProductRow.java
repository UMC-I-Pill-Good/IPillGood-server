package com.ipillgood.server.domain.intake.repository;

import java.time.LocalDate;

public record TodayScheduledProductRow(
        Long activeProductId,
        Long memberProductId,
        Long productId,
        String productName,
        LocalDate scheduleAnchorOn,
        Short frequencyIntervalDays
) {
}
