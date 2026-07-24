package com.ipillgood.server.domain.auth.dto;

import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import lombok.Builder;

import java.time.LocalDateTime;

public class AuthResponse {

    /**
     * 로컬 회원가입 응답
     */
    @Builder
    public record SignUp(
            Long memberId,
            String nickname,
            String username,
            String email,
            String profileImageUrl,
            Boolean onboardingCompleted,
            LocalDateTime createdAt
    ) {
    }

    /**
     * 로컬 로그인 응답
     */
    @Builder
    public record Login(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            Long memberId,
            Boolean onboardingCompleted
    ) {
    }

    /**
     * 소셜 로그인 응답
     * - 로그인 성공: 토큰 필드
     * - 가입 필요: signupRequired
     * - 연동 필요: accountLinkRequired + accountLinkToken
     */
    @Builder
    public record SocialLogin(
            Boolean signupRequired,
            Boolean accountLinkRequired,
            String accountLinkToken,
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            Long memberId,
            Boolean onboardingCompleted
    ) {
    }

    /**
     * 소셜 회원가입 응답
     */
    @Builder
    public record SocialSignUp(
            Long memberId,
            SocialProvider provider,
            String nickname,
            String email,
            String profileImageUrl,
            Boolean onboardingCompleted,
            LocalDateTime createdAt
    ) {
    }

    /**
     * 소셜 계정 연동 응답
     * 연동 즉시 로그인 처리하므로 로그인 토큰을 함께 발급
     */
    @Builder
    public record SocialLink(
            Boolean linked,
            SocialProvider provider,
            LocalDateTime linkedAt,
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            Long memberId,
            Boolean onboardingCompleted
    ) {
    }
}
