package com.ipillgood.server.domain.intake.repository;

import java.time.LocalDateTime;

public record TodayIntakeRecordRow(
        Long activeProductId,
        Boolean taken,
        LocalDateTime takenAt
) {
}
