package com.ipillgood.server.domain.member.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
    ACTIVE("활성"),
    BANNED("정지");

    private final String label;
}
