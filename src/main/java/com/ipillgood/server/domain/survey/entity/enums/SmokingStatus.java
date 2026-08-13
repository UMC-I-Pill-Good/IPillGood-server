package com.ipillgood.server.domain.survey.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SmokingStatus {
    NONE("비흡연"),
    PAST("과거 흡연"),
    CURRENT("현재 흡연");

    private final String label;
}
