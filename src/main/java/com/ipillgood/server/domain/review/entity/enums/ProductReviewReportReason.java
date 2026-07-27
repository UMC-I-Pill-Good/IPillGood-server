package com.ipillgood.server.domain.review.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductReviewReportReason {
    AD_PROMOTION("광고·홍보"),
    ABUSE("욕설·비방"),
    FALSE_INFO("허위 정보"),
    PERSONAL_INFO("개인정보"),
    ETC("기타");

    private final String label;
}
