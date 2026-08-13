package com.ipillgood.server.domain.ingredient.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContraindicationType {
    MEDICATION("복용약"),
    DRINKING("음주"),
    PREGNANCY("임신"),
    SMOKING("흡연"),
    UNDERLYING_DISEASE("기저질환"),
    ALLERGY("알러지");

    private final String label;
}
