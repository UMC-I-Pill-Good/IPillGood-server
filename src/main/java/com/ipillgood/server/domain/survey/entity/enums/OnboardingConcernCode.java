package com.ipillgood.server.domain.survey.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OnboardingConcernCode {
    FATIGUE("피로"),
    IMMUNITY("면역"),
    SLEEP_QUALITY("수면의 질"),
    GUT_HEALTH("장 건강"),
    SKIN_HEALTH("피부 건강"),
    WEIGHT_MANAGEMENT("체중 관리"),
    EYE_HEALTH("눈 건강"),
    BONE_JOINT("뼈·관절"),
    BLOOD_PRESSURE_VESSEL("혈압·혈관"),
    STRESS("스트레스"),
    ANTIOXIDANT("항산화"),
    HAIR_HEALTH("모발 건강"),
    WOMENS_HEALTH("여성 건강"),
    EXERCISE_PERFORMANCE("운동수행능력");

    private final String label;
}
