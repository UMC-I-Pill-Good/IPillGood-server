package com.ipillgood.server.domain.auth.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 소셜 제공자 연동 설정을 담는 클래스 (application.yml의 social.*)
 * 제공자 API 주소를 코드로 쓰지 않고 설정으로 분리
 * 주소 변경 시 배포 설정만 수정 목적
 */
@ConfigurationProperties(prefix = "social")
public record SocialProperties(
        Kakao kakao,
        Naver naver
) {

    /**
     * 카카오 연동 설정
     */
    public record Kakao(
            String userInfoUri,
            String authorizeUri,
            String tokenUri,
            String clientId,
            String clientSecret,
            String redirectUri
    ) {
    }

    /**
     * 네이버 연동 설정
     */
    public record Naver(
            String userInfoUri,
            String authorizeUri,
            String tokenUri,
            String clientId,
            String clientSecret,
            String redirectUri
    ) {
    }
}
