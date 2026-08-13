package com.ipillgood.server.domain.review.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductReviewSort {
    LATEST("최신순"),
    LIKE_COUNT_DESC("좋아요순");

    private final String label;
}
