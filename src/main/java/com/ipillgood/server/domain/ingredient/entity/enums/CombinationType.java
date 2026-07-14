package com.ipillgood.server.domain.ingredient.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CombinationType {
    GOOD("좋음"),
    BAD("나쁨");

    private final String label;
}
