package com.ipillgood.server.domain.healthconcern.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MajorCategory {

    NERVOUS("신경계"),
    SENSORY("감각계"),
    DIGESTIVE_METABOLIC("소화·대사계"),
    ENDOCRINE("내분비계"),
    CARDIOVASCULAR("심혈관계"),
    IMMUNE("신체방어 및 면역계"),
    MUSCULAR("근육계"),
    REPRODUCTIVE_URINARY("생식·비뇨계");

    private final String label;
}
