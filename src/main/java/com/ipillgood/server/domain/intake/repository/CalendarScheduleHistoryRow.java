package com.ipillgood.server.domain.intake.repository;

import java.time.LocalDate;

public record CalendarScheduleHistoryRow(
        Long activeProductId,
        LocalDate startedOn,
        LocalDate stoppedOn,
        LocalDate scheduleAnchorOn,
        Short frequencyIntervalDays,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
