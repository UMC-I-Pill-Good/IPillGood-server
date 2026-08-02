package com.ipillgood.server.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Year;

@Getter
@RequiredArgsConstructor
public enum AgeGroup {
    TEENS("10대"),
    TWENTIES("20대"),
    THIRTIES("30대"),
    FORTIES("40대"),
    FIFTIES_PLUS("50대 이상"),
    ALL("전체");

    private final String label;

    /**
     * 설문에서 받은 출생연도를 연 나이 기준 연령대로 변환한다.
     */
    public static AgeGroup fromBirthYear(Short birthYear) {
        if (birthYear == null) {
            return null;
        }

        int age = Year.now().getValue() - birthYear;
        if (age < 20) {
            return TEENS;
        }
        if (age < 30) {
            return TWENTIES;
        }
        if (age < 40) {
            return THIRTIES;
        }
        if (age < 50) {
            return FORTIES;
        }
        return FIFTIES_PLUS;
    }
}
