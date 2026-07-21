package com.ipillgood.server.domain.auth.client;

import com.ipillgood.server.domain.auth.client.dto.SocialProfile;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;

/**
 * Social Provider 프로필 조회 규약
 */
public interface SocialProfileClient {

    /**
     * 소셜 제공자(카카오 또는 네이버)
     */
    SocialProvider provider();

    /**
     * 액세스 토큰으로 소셜 제공자로부터 사용자 정보 조회
     * 소셜 로그인/소셜 회원가입/소셜 계정 연동 3가지 상황에서 실행
     * 토큰이 만료·위조됐거나 우리 앱에서 발급된 것이 아니면 AuthException을 던짐
     */
    SocialProfile fetch(String providerAccessToken);
}
