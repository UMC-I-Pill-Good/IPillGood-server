package com.ipillgood.server.domain.notification.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    INTAKE("복용"),
    CONDITION_CHECK("컨디션 체크");

    private final String label;
}
