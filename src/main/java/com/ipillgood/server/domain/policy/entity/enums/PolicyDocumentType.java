package com.ipillgood.server.domain.policy.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PolicyDocumentType {
    SERVICE_TERMS("서비스 이용약관"),
    PRIVACY_POLICY("개인정보 처리방침"),
    PRIVACY_COLLECTION("개인정보 수집 및 이용"),
    HEALTH_INFO_COLLECTION("건강정보 수집 및 이용"),
    MARKETING("마케팅 정보 수신");

    private final String label;
}
