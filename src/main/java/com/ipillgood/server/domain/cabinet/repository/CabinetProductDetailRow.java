package com.ipillgood.server.domain.cabinet.repository;

import com.ipillgood.server.domain.intake.entity.enums.IntakeFrequency;

import java.time.LocalDate;
import java.time.LocalTime;

public record CabinetProductDetailRow(
        Long memberProductId,
        Long productId,
        String brand,
        String productName,
        Long activeProductId,
        LocalDate startedOn,
        Boolean notificationEnabled,
        LocalTime intakeTime,
        IntakeFrequency frequency,
        Short frequencyIntervalDays,
        LocalDate scheduleAnchorOn,
        Boolean hasMyReview
) {
}
