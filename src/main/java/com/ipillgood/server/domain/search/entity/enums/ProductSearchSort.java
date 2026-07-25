package com.ipillgood.server.domain.search.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductSearchSort {

    REVIEW_COUNT("후기 많은 순"),
    RATING("평점 높은 순");

    private final String label;
}
