package com.ipillgood.server.domain.auth.converter;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.MemberSocialAccount;
import com.ipillgood.server.domain.member.entity.Role;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;

import java.util.function.Function;

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
                .profileImageKey(Member.randomProfileImageKey())
                .build();
    }

    /**
     * 1. 로컬 회원가입 - 멤버 엔티티 -> DTO 변환
     */
    public static AuthResponse.SignUp toSignUpResponse(Member member, Function<String, String> imageUrlResolver) {
        return AuthResponse.SignUp.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .username(member.getUsername())
                .email(member.getEmail())
                .profileImageUrl(imageUrlResolver.apply(member.getProfileImageKey()))
                .onboardingCompleted(member.getOnboardingCompletedAt() != null)
                .createdAt(member.getCreatedAt())
                .build();
    }

    /**
     * 2. 로컬 로그인 - 발급된 토큰 -> DTO 변환
     * 리프레시 토큰 포함 X
     */
    public static AuthResponse.Login toLoginResponse(Member member, String accessToken, long expiresIn) {
        return AuthResponse.Login.builder()
                .accessToken(accessToken)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(expiresIn)
                .memberId(member.getId())
                .onboardingCompleted(member.getOnboardingCompletedAt() != null)
                .build();
    }

    /**
     * 소셜 회원가입 - 저장된 회원 엔티티 + JWT 토큰 -> DTO 변환
     * 회원가입과 동시에 로그인 처리하므로 토큰을 포함함
     */
    public static AuthResponse.SocialSignUp toSocialSignUpResponse(Member member, SocialProvider provider,
                                                                   Function<String, String> imageUrlResolver,
                                                                   String accessToken, long expiresIn) {
        return AuthResponse.SocialSignUp.builder()
                .memberId(member.getId())
                .provider(provider)
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImageUrl(imageUrlResolver.apply(member.getProfileImageKey()))
                .onboardingCompleted(member.getOnboardingCompletedAt() != null)
                .createdAt(member.getCreatedAt())
                .accessToken(accessToken)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(expiresIn)
                .build();
    }

    /**
     * 소셜 계정 연동 - 연동 결과 + 발급된 로그인 토큰 -> DTO 변환
     * 연동 즉시 로그인 처리하므로 토큰을 함께 담음
     */
    public static AuthResponse.SocialLink toSocialLinkResponse(Member member, MemberSocialAccount socialAccount,
                                                               String accessToken, long expiresIn) {
        return AuthResponse.SocialLink.builder()
                .linked(true)
                .provider(socialAccount.getProvider())
                .linkedAt(socialAccount.getLinkedAt())
                .accessToken(accessToken)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(expiresIn)
                .memberId(member.getId())
                .onboardingCompleted(member.getOnboardingCompletedAt() != null)
                .build();
    }
}
