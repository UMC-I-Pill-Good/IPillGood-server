package com.ipillgood.server.domain.support.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FaqCategory {
    RECOMMENDATION_INGREDIENT("추천 성분"),
    INTAKE("복용"),
    NOTIFICATION("알림"),
    ETC("기타");

    private final String label;
}
