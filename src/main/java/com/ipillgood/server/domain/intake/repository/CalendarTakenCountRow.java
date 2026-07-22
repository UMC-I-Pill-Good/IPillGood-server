package com.ipillgood.server.domain.intake.repository;

import java.time.LocalDate;

public record CalendarTakenCountRow(
        LocalDate intakeOn,
        Long takenCount
) {
}
