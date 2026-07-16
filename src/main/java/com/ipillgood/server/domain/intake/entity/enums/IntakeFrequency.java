package com.ipillgood.server.domain.intake.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IntakeFrequency {
    EVERY_DAY("매일"),
    EVERY_2_DAYS("2일마다"),
    EVERY_3_DAYS("3일마다"),
    WEEKLY("매주"),
    EVERY_2_WEEKS("2주마다");

    private final String label;
}
