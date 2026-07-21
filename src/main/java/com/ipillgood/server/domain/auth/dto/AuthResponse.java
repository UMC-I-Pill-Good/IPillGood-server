package com.ipillgood.server.domain.auth.dto;

import lombok.Builder;

import java.time.LocalDateTime;

public class AuthResponse {

    // 회원가입 응답
    @Builder
    public record SignUp(
            Long memberId,
            String nickname,
            String username,
            String email,
            String profileImageKey,
            Boolean onboardingCompleted,
            LocalDateTime createdAt
    ) {
    }

    // 로그인 응답
    @Builder
    public record Login(
            String accessToken,
            String refreshToken
    ) {
    }
}
