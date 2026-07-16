package com.ipillgood.server.domain.survey.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExerciseFrequency {
    RARELY("거의 안 함"),
    WEEK_1_2("주 1~2회"),
    WEEK_3_PLUS("주 3회 이상");

    private final String label;
}
