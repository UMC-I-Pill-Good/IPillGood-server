package com.ipillgood.server.domain.notification.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationDeliveryStatus {
    PENDING("대기"),
    SENT("발송 완료"),
    FAILED("발송 실패"),
    RETRY_FAILED("재시도 실패"),
    SKIPPED("스킵");

    private final String label;
}
