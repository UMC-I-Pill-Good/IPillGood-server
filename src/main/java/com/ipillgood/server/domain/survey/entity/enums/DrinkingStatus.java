package com.ipillgood.server.domain.survey.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DrinkingStatus {
    NONE("음주 안 함"),
    OCCASIONAL("가끔"),
    FREQUENT("자주");

    private final String label;
}
