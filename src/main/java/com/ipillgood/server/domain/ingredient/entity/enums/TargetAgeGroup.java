package com.ipillgood.server.domain.ingredient.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetAgeGroup {
    TEENS("10대"),
    TWENTIES("20대"),
    THIRTIES("30대"),
    FORTIES("40대"),
    FIFTIES_PLUS("50대 이상"),
    ALL("전체");

    private final String label;
}
