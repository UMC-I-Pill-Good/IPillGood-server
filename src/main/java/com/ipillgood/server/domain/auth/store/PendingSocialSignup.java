package com.ipillgood.server.domain.auth.store;

import com.ipillgood.server.domain.member.entity.enums.SocialProvider;

/**
 * 카카오/네이버에서 조회한 프로필을 [아필굿 약관 동의 화면] 진행하는 대기 시점에 사용하는 정보를 담은 DTO 클래스
 */
public record PendingSocialSignup(
        SocialProvider provider,
        String providerUserId,
        String email,
        String nickname
) {
}
