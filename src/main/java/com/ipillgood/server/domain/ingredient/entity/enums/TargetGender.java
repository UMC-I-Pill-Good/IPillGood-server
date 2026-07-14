package com.ipillgood.server.domain.ingredient.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetGender {
    ALL("모두"),
    FEMALE("여성"),
    MALE("남성");

    private final String label;
}
