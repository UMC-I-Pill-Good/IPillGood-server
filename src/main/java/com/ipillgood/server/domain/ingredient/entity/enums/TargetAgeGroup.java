package com.ipillgood.server.domain.ingredient.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetAgeGroup {
    ALL("전연령"),
    AGE_20_UP("20대 이상"),
    AGE_40_UP("40대 이상");

    private final String label;
}
