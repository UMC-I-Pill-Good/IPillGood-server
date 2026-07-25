package com.ipillgood.server.domain.auth.store;

import com.ipillgood.server.domain.member.entity.enums.SocialProvider;

/**
 * 계정 연동 시점에 사용하는, 대기 정보를 담은 DTO 클래스
 * 사용자가 연동 팝업에서 결정할 때까지 저장소에 잠시 머물렀다가, 연동 요청이 오면 꺼내 사용
 */
public record PendingSocialLink(
        Long memberId,
        SocialProvider provider,
        String providerUserId,
        String providerEmail
) {
}
