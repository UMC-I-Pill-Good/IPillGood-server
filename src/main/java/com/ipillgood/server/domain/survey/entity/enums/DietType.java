package com.ipillgood.server.domain.survey.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DietType {
    MIXED("일반식"),
    VEGETARIAN("채식 위주"),
    MEAT_BASED("육류 위주");

    private final String label;
}
