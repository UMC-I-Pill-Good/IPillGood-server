package com.ipillgood.server.domain.survey.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobType {
    OFFICE("사무직"),
    PROFESSIONAL("전문직"),
    SERVICE("서비스직"),
    PRODUCTION_TECH("생산·기술직"),
    SELF_EMPLOYED("자영업"),
    STUDENT("학생"),
    HOUSEWIFE("주부"),
    ATHLETE_TRAINER("운동선수/트레이너");

    private final String label;
}
