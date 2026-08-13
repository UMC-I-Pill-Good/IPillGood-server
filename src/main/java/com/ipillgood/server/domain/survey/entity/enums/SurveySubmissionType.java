package com.ipillgood.server.domain.survey.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SurveySubmissionType {
    INITIAL("최초 설문"),
    REVISION("재설문");

    private final String label;
}
