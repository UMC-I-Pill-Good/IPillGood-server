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
import com.ipillgood.server.domain.member.entity.MemberSocialAccount;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import com.ipillgood.server.domain.member.service.MemberService;
import com.ipillgood.server.domain.policy.service.PolicyService;
import com.ipillgood.server.global.s3.S3Service;
import com.ipillgood.server.global.security.jwt.JwtProvider;
import com.ipillgood.server.global.security.jwt.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 소셜 로그인/회원가입/계정 연동 흐름을 담당하는 클래스 (판정과 토큰 발급만 책임)
 * 외부 API 호출이 포함되므로 클래스 단위 트랜잭션은 걸지 않고, 저장이 필요한 메서드만 개별로 건다
 */
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final SocialProfileClientResolver socialProfileClientResolver;
    private final AccountLinkTokenStore accountLinkTokenStore;
    private final MemberService memberService;
    private final PolicyService policyService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final S3Service s3Service;

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
     * 소셜 회원가입 요청 시 실행
     * 소셜 프로필을 조회해 신규 회원과 약관 동의 저장을 한 트랜잭션으로 처리
     * 외부 API 호출이 트랜잭션에 포함 -> DB 커넥션 오래 사용 (단점)
     */
    @Transactional
    public AuthResponse.SocialSignUp signUp(SocialProvider provider, AuthRequest.SocialSignUp request) {

        // 소셜 제공자에게 사용자 정보 조회 (액세스 토큰 유효한지 검증)
        SocialProfile profile = socialProfileClientResolver.resolve(provider)
                .fetch(request.providerAccessToken());

        // 1. 이메일이 없으면 회원을 생성할 수 없으므로 중단
        if (!StringUtils.hasText(profile.email())) {
            throw new AuthException(AuthErrorCode.SOCIAL_EMAIL_NOT_FOUND);
        }

        // 2. 닉네임이 없으면 회원 닉네임을 정할 수 없으므로 중단
        if (!StringUtils.hasText(profile.nickname())) {
            throw new AuthException(AuthErrorCode.SOCIAL_NICKNAME_NOT_FOUND);
        }

        // 3. 이미 연동된 소셜 계정이면 중복 가입 차단
        if (memberService.isSocialAccountLinked(provider, profile.providerUserId())) {
            throw new AuthException(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS);
        }

        // 4. 이미 사용 중인 이메일이면 차단 - 비정상적인 signUp 호출 (정상 흐름은 로그인 단계에서 연동으로 안내됨)
        memberService.findByEmail(profile.email()).ifPresent(member -> {

            // 소셜 전용 계정이면 해당 소셜로 로그인하도록 안내
            if (member.isSocialOnly()) {
                throw new AuthException(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS);
            }

            // 로컬 계정이면 해당 이메일로 로그인하도록 안내
            throw new AuthException(AuthErrorCode.ACCOUNT_LINK_REQUIRED);
        });

        // 5. 회원 + 소셜 계정 저장 후 약관 동의 이력 저장
        Member member = memberService.createSocialMember(
                profile.email(), profile.nickname(), provider, profile.providerUserId());
        policyService.agreeToPolicies(member, request.policyAgreements());

        return AuthConverter.toSocialSignUpResponse(member, provider, s3Service::getPublicUrl);
    }

    /**
     * 소셜 계정 연동 요청 시 실행
     * 임시 토큰의 기존 회원에 소셜 계정을 붙이고 로그인 토큰을 발급
     */
    @Transactional
    public AuthResponse.SocialLink link(SocialProvider provider, AuthRequest.SocialLink request) {

        // 1. 임시 토큰에서 연동 대기 정보를 꺼냄 (없거나 만료됐으면 실패)
        PendingSocialLink pending = accountLinkTokenStore.consume(request.accountLinkToken())
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_LINK_TOKEN_INVALID));

        // 2. URL provider와 토큰에 담긴 provider가 다르면 차단
        // 로그인, 회원가입과 다르게 연동 처리는 토큰 속 provider 값을 진실로 판단
        if (pending.provider() != provider) {
            throw new AuthException(AuthErrorCode.ACCOUNT_LINK_TOKEN_INVALID);
        }

        // 3. 연동 대상 회원 조회
        Member member = memberService.findById(pending.memberId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_LINK_TOKEN_INVALID));

        // 4. 이미 연동된 소셜 계정이면 중복 연동 차단 (같은 토큰으로 동시 요청 방어)
        if (memberService.isSocialAccountLinked(provider, pending.providerUserId())) {
            throw new AuthException(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS);
        }

        // 5. 기존 회원에 소셜 계정 연동
        MemberSocialAccount socialAccount = memberService.linkSocialAccount(
                member, provider, pending.providerUserId(), pending.providerEmail());

        // 6. 연동 즉시 로그인 처리 - 신규 세션(기기) 발급 후 토큰 발급, 재발급 검증용으로 저장
        String role = member.getRole().name();
        String sessionId = jwtProvider.generateSessionId();
        String accessToken = jwtProvider.createAccessToken(member.getId(), role, sessionId);
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), role, sessionId);
        refreshTokenStore.save(member.getId(), sessionId, refreshToken, jwtProvider.getRefreshTokenValidity());

        return AuthConverter.toSocialLinkResponse(member, socialAccount, accessToken, refreshToken,
                jwtProvider.getAccessTokenExpiresIn());
    }

    /**
     * 로그인 토큰 발급 (로컬 로그인과 동일한 절차)
     */
    private AuthResponse.SocialLogin issueLoginTokens(Member member) {
        String role = member.getRole().name();
        String sessionId = jwtProvider.generateSessionId();
        String accessToken = jwtProvider.createAccessToken(member.getId(), role, sessionId);
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), role, sessionId);
        refreshTokenStore.save(member.getId(), sessionId, refreshToken, jwtProvider.getRefreshTokenValidity());

        return AuthConverter.toSocialLoginResponse(member, accessToken, refreshToken,
                jwtProvider.getAccessTokenExpiresIn());
    }
}
