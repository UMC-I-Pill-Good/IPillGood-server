package com.ipillgood.server.domain.review.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewReportStatus {
    PENDING("처리 대기"),
    DELETED("삭제 처리"),
    MAINTAINED("유지 처리"),
    HIDDEN("숨김 처리");

    private final String label;
}
