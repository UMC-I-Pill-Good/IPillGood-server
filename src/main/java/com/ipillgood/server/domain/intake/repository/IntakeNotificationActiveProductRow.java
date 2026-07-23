package com.ipillgood.server.domain.intake.repository;

import java.time.LocalTime;

public record IntakeNotificationActiveProductRow(
        Long activeProductId,
        Long memberProductId,
        Long productId,
        String productName,
        Boolean notificationEnabled,
        LocalTime intakeTime
) {
}
