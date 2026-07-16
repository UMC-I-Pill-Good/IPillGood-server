package com.ipillgood.server.domain.member.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {
    KAKAO("카카오"),
    NAVER("네이버");

    private final String label;
}
