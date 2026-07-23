package com.ipillgood.server.domain.auth.converter;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.Role;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;

public class AuthConverter {

    // 응답에 내려주는 토큰 타입 (Authorization 헤더에 "Bearer {accessToken}" 형태로 사용)
    private static final String TOKEN_TYPE_BEARER = "Bearer";

    /**
     * 1. 로컬 회원가입 - 멤버 엔티티 생성
     */
    public static Member toMember(AuthRequest.SignUp request, String encodedPassword) {
        return Member.builder()
                .nickname(request.nickname())
                .username(request.username())
                .email(request.email())
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }

    /**
     * 1. 로컬 회원가입 - 멤버 엔티티 -> DTO 변환
     */
    public static AuthResponse.SignUp toSignUpResponse(Member member) {
        return AuthResponse.SignUp.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .username(member.getUsername())
                .email(member.getEmail())
                .profileImageKey(member.getProfileImageKey())
                .onboardingCompleted(member.getOnboardingCompletedAt() != null)
                .createdAt(member.getCreatedAt())
                .build();
    }

    /**
     * 2. 로컬 로그인 - 발급된 토큰 -> DTO 변환
     */
    public static AuthResponse.Login toLoginResponse(Member member, String accessToken,
                                                     String refreshToken, long expiresIn) {
        return AuthResponse.Login.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(expiresIn)
                .memberId(member.getId())
                .onboardingCompleted(member.getOnboardingCompletedAt() != null)
                .build();
    }

    /**
     * 3. 소셜 로그인 - 이미 연동된 소셜 계정이라 바로 로그인 처리
     * 로컬 로그인과 동일한 응답 구조 + 판정 플래그 2개만 추가
     */
    public static AuthResponse.SocialLogin toSocialLoginResponse(Member member, String accessToken,
                                                                 String refreshToken, long expiresIn) {
        return AuthResponse.SocialLogin.builder()
                .signupRequired(false)
                .accountLinkRequired(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(expiresIn)
                .memberId(member.getId())
                .onboardingCompleted(member.getOnboardingCompletedAt() != null)
                .build();
    }

    /**
     * 4. 소셜 로그인 - 완전 신규 사용자 -> 회원가입 필요 (아직 토큰 발급 X)
     */
    public static AuthResponse.SocialLogin toSignUpRequiredResponse() {
        return AuthResponse.SocialLogin.builder()
                .signupRequired(true)
                .accountLinkRequired(false)
                .build();
    }

    /**
     * 5. [로컬 이메일이 존재할 때 또는 다른 소셜 이메일이 존재할 때]
     * 소셜 로그인 - 같은 이메일의 기존 회원이 있어 연동 동의가 필요
     */
    public static AuthResponse.SocialLogin toAccountLinkRequiredResponse(String accountLinkToken) {
        return AuthResponse.SocialLogin.builder()
                .signupRequired(false)
                .accountLinkRequired(true)

                // 연동 요청에 쓸 임시 토큰
                .accountLinkToken(accountLinkToken)
                .build();
    }

    /**
     * 소셜 회원가입 - 저장된 회원 엔티티 -> DTO 변환
     * 자동 로그인하지 않으므로 토큰은 담지 않음
     */
    public static AuthResponse.SocialSignUp toSocialSignUpResponse(Member member, SocialProvider provider) {
        return AuthResponse.SocialSignUp.builder()
                .memberId(member.getId())
                .provider(provider)
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImageKey(member.getProfileImageKey())
                .onboardingCompleted(member.getOnboardingCompletedAt() != null)
                .createdAt(member.getCreatedAt())
                .build();
    }
}
