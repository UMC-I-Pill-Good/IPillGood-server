package com.ipillgood.server.domain.healthconcern.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory.*;

@RequiredArgsConstructor
@Getter
public enum MinorCategory {

    // 신경계
    COGNITION("인지 기능/기억력", NERVOUS),
    TENSION("긴장", NERVOUS),
    SLEEP_QUALITY("수면의 질", NERVOUS),
    FATIGUE("피로", NERVOUS),

    // 감각계
    TEETH("치아", SENSORY),
    EYE("눈", SENSORY),
    SKIN("피부", SENSORY),

    // 소화·대사계
    LIVER("간", DIGESTIVE_METABOLIC),
    STOMACH("위", DIGESTIVE_METABOLIC),
    INTESTINE("장", DIGESTIVE_METABOLIC),
    BODY_FAT("체지방", DIGESTIVE_METABOLIC),
    CALCIUM_ABSORPTION("칼슘 흡수", DIGESTIVE_METABOLIC),

    // 내분비계
    BLOOD_SUGAR("혈당", ENDOCRINE),
    MENOPAUSE_FEMALE("갱년기 여성", ENDOCRINE),
    MENOPAUSE_MALE("갱년기 남성", ENDOCRINE),
    PMS("월경 전 불편한 상태", ENDOCRINE),

    // 심혈관계
    TRIGLYCERIDE("혈중 중성지방", CARDIOVASCULAR),
    CHOLESTEROL("콜레스테롤", CARDIOVASCULAR),
    BLOOD_PRESSURE("혈압", CARDIOVASCULAR),
    BLOOD_CIRCULATION("혈행", CARDIOVASCULAR),

    // 신체방어 및 면역계
    IMMUNITY("면역", IMMUNE),
    ANTIOXIDANT("항산화", IMMUNE),

    // 근육계
    JOINT("관절", MUSCULAR),
    BONE("뼈", MUSCULAR),
    MUSCLE_STRENGTH("근력", MUSCULAR),
    EXERCISE_PERFORMANCE("운동수행능력", MUSCULAR),

    // 생식·비뇨계
    PROSTATE("전립선", REPRODUCTIVE_URINARY),
    URINATION("배뇨", REPRODUCTIVE_URINARY),
    URINARY_TRACT("요로", REPRODUCTIVE_URINARY);

    private final String label;
    private final MajorCategory majorCategory;
}
