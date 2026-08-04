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
            String tokenType,
            Long expiresIn,
            Long memberId,
            Boolean onboardingCompleted
    ) {
    }

    /**
     * 소셜 회원가입 응답
     * 회원가입과 동시에 로그인 처리되므로 로그인 토큰을 함께 담음
     */
    @Builder
    public record SocialSignUp(
            Long memberId,
            SocialProvider provider,
            String nickname,
            String email,
            String profileImageUrl,
            Boolean onboardingCompleted,
            LocalDateTime createdAt,
            String accessToken,
            String tokenType,
            Long expiresIn
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
            String tokenType,
            Long expiresIn,
            Long memberId,
            Boolean onboardingCompleted
    ) {
    }
}
