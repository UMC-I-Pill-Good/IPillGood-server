package com.ipillgood.server.domain.member.dto;

import lombok.Builder;

import java.util.List;

public class MemberResponse {

    /**
     * 내 정보 조회 응답
     */
    @Builder
    public record MyInfo(
            Long memberId,
            String nickname,
            String profileImageUrl,
            List<String> loginProviders,
            Boolean onboardingCompleted
    ) {
    }
}
