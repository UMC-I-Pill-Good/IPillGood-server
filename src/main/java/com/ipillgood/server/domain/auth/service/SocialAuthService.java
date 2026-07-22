package com.ipillgood.server.domain.auth.service;

import com.ipillgood.server.domain.auth.client.SocialProfileClientResolver;
import com.ipillgood.server.domain.auth.client.dto.SocialProfile;
import com.ipillgood.server.domain.auth.code.AuthErrorCode;
import com.ipillgood.server.domain.auth.converter.AuthConverter;
import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.auth.exception.AuthException;
import com.ipillgood.server.domain.auth.store.AccountLinkTokenStore;
import com.ipillgood.server.domain.auth.store.PendingSocialLink;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import com.ipillgood.server.domain.member.service.MemberService;
import com.ipillgood.server.global.security.jwt.JwtProvider;
import com.ipillgood.server.global.security.jwt.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 소셜 로그인/회원가입/계정 연동 흐름을 담당하는 클래스 (판정과 토큰 발급만 책임)
 * 외부 API 호출 때문에 트랜잭션을 걸지 않고, AuthService와 분리
 */
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final SocialProfileClientResolver socialProfileClientResolver;
    private final AccountLinkTokenStore accountLinkTokenStore;
    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;

    /**
     * 소셜 로그인 요청 시 실행 - 4가지 케이스
     * - 1. 이미 연동된 소셜 계정이면 로그인 토큰 발급
     * - 2. 이메일을 확인할 수 없으면 중단
     * - 3. 같은 이메일의 기존 회원 존재 -> 연동 동의를 위한 임시 토큰 발급
     * - 4. 완전 신규 사용자 -> 회원가입 필요
     */
    public AuthResponse.SocialLogin login(SocialProvider provider, AuthRequest.SocialLogin request) {

        // 소셜 제공자에게 사용자 정보 조회 (액세스 토큰 유효한지 검증)
        SocialProfile profile = socialProfileClientResolver.resolve(provider)
                .fetch(request.providerAccessToken());

        // 1. 이미 연동된 소셜 계정 -> 로그인 토큰 발급
        Optional<Member> linkedMember = memberService.findBySocialAccount(provider, profile.providerUserId());
        if (linkedMember.isPresent()) {
            return issueLoginTokens(linkedMember.get());
        }

        // 2. 이메일이 없으면 기존 회원 존재 여부를 판단할 수 없으므로 중단
        // (소셜 로그인 중 이메일 미동의 또는 이메일 없는 경우 등)
        if (!StringUtils.hasText(profile.email())) {
            throw new AuthException(AuthErrorCode.SOCIAL_EMAIL_NOT_FOUND);
        }

        // 3. 같은 이메일의 기존 회원이 있는 경우 -> 연동 여부 확인
        Optional<Member> existingMember = memberService.findByEmail(profile.email());
        if (existingMember.isPresent()) {
            String accountLinkToken = accountLinkTokenStore.issue(new PendingSocialLink(
                    existingMember.get().getId(), provider, profile.providerUserId(), profile.email()));
            return AuthConverter.toAccountLinkRequiredResponse(accountLinkToken);
        }

        // 4. 소셜 계정도 기존 회원도 없으면 신규 사용자
        return AuthConverter.toSignUpRequiredResponse();
    }

    /**
     * 로그인 토큰 발급 (로컬 로그인과 동일한 절차)
     */
    private AuthResponse.SocialLogin issueLoginTokens(Member member) {
        String role = member.getRole().name();
        String accessToken = jwtProvider.createAccessToken(member.getId(), role);
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), role);
        refreshTokenStore.save(member.getId(), refreshToken, jwtProvider.getRefreshTokenValidity());

        return AuthConverter.toSocialLoginResponse(member, accessToken, refreshToken,
                jwtProvider.getAccessTokenExpiresIn());
    }
}
