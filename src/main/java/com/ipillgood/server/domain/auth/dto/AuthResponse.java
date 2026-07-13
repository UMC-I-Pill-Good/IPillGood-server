package com.ipillgood.server.domain.auth.dto;

import lombok.Builder;

public class AuthResponse {

    // 회원가입 응답
    @Builder
    public record SignUp(
            Long id,
            String nickname,
            String username,
            String email
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
