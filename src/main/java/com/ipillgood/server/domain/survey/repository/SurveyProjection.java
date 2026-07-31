package com.ipillgood.server.domain.survey.repository;

import com.ipillgood.server.global.enums.Gender;

public class SurveyProjection {

    public record MemberProfile(
            Long memberId,
            Short birthYear,
            Gender gender
    ) {
    }
}
