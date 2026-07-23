package com.ipillgood.server.domain.intake.repository;

import com.ipillgood.server.domain.intake.entity.enums.IntakeFrequency;

import java.time.LocalDate;
import java.time.LocalTime;

public record ActiveProductSettingsRow(
        Long activeProductId,
        Long memberProductId,
        Long productId,
        String brand,
        String productName,
        LocalDate startedOn,
        Boolean notificationEnabled,
        LocalTime intakeTime,
        IntakeFrequency frequency,
        Short frequencyIntervalDays,
        LocalDate scheduleAnchorOn,
        Long ingredientCount,
        String singleIngredientImageKey
) {
}
