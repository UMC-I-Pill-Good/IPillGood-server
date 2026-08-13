package com.ipillgood.server.domain.intake.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IntakeFrequency {
    EVERY_DAY("매일", (short) 1),
    EVERY_2_DAYS("2일에 한 번", (short) 2),
    EVERY_3_DAYS("3일에 한 번", (short) 3),
    WEEKLY("일주일에 한 번", (short) 7),
    EVERY_2_WEEKS("2주일에 한 번", (short) 14);

    private final String label;
    private final Short intervalDays;
}
