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

    // 회원 닉네임 컬럼 길이
    int NICKNAME_MAX_LENGTH = 10;

    // 회원 닉네임 허용 문자 (한글/영문/숫자가 아닌 모든 것 제외)
    String NICKNAME_DISALLOWED_CHARS = "[^가-힣a-zA-Z0-9]";

    /**
     * 소셜 닉네임을 정책에 맞게 정제
     */
    default String sanitizeNickname(String nickname) {
        if (nickname == null) {
            return null;
        }

        // 특수문자·공백·이모지 제거
        String sanitized = nickname.replaceAll(NICKNAME_DISALLOWED_CHARS, "");

        // 10자 넘을 경우 10자로 자름
        if (sanitized.length() > NICKNAME_MAX_LENGTH) {
            sanitized = sanitized.substring(0, NICKNAME_MAX_LENGTH);
        }

        // 닉네임 조건 정제 후 빈 값이면 null 처리
        return sanitized.isEmpty() ? null : sanitized;
    }
}
