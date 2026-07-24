package com.ipillgood.server.domain.member.converter;

import com.ipillgood.server.domain.member.dto.MemberResponse;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MemberConverter {

    private static final String LOCAL_PROVIDER = "LOCAL";

    private MemberConverter() {
    }

    /**
     * 회원 엔티티 + 로컬 계정 확인 + 연동 소셜 목록 -> DTO 변환
     */
    public static MemberResponse.MyInfo toMyInfo(Member member, List<SocialProvider> socialProviders,
                                                 Function<String, String> imageUrlResolver) {
        List<String> loginProviders = new ArrayList<>();
        if (!member.isSocialOnly()) {
            loginProviders.add(LOCAL_PROVIDER);
        }
        socialProviders.forEach(provider -> loginProviders.add(provider.name()));

        return MemberResponse.MyInfo.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImageUrl(imageUrlResolver.apply(member.getProfileImageKey()))
                .loginProviders(loginProviders)
                .onboardingCompleted(member.getOnboardingCompletedAt() != null)
                .build();
    }
}
