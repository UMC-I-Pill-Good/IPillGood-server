package com.ipillgood.server.domain.intake.repository;

import java.time.LocalDate;
import java.time.LocalTime;

public record IntakeNotificationDeliveryProductRow(
        Long memberId,
        Long activeProductId,
        String productName,
        LocalDate scheduleAnchorOn,
        Short frequencyIntervalDays,
        LocalTime intakeTime
) {
}
