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
import com.ipillgood.server.domain.auth.store.PendingSocialSignup;
import com.ipillgood.server.domain.auth.store.SocialSignupTokenStore;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.MemberSocialAccount;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import com.ipillgood.server.domain.member.service.MemberService;
import com.ipillgood.server.domain.policy.service.PolicyService;
import com.ipillgood.server.global.s3.S3Service;
import com.ipillgood.server.global.security.jwt.CookieUtil;
import com.ipillgood.server.global.security.jwt.JwtProvider;
import com.ipillgood.server.global.security.jwt.RefreshTokenStore;
import jakarta.servlet.http.HttpServletResponse;
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
    private final SocialSignupTokenStore socialSignupTokenStore;
    private final MemberService memberService;
    private final PolicyService policyService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final S3Service s3Service;
    private final CookieUtil cookieUtil;

    /**
     * 소셜 로그인 콜백에서 실행 - 4가지 케이스
     * - 1. 이미 연동된 소셜 계정이면 로그인 토큰 발급
     * - 2. 이메일을 확인할 수 없으면 중단
     * - 3. 같은 이메일의 기존 회원 존재 -> 연동 동의를 위한 임시 토큰 발급
     * - 4. 완전 신규 사용자 -> 회원가입 필요 - 회원가입 대기 임시 토큰 발급
     */
    public SocialCallbackResult handleCallback(SocialProvider provider, String code, String state,
                                               HttpServletResponse response) {

        // 인가 코드로 액세스 토큰 교환 후 소셜 제공자에게 사용자 정보 조회
        SocialProfile profile = socialProfileClientResolver.resolve(provider).fetchByCode(code, state);

        // 1. 이미 연동된 소셜 계정 -> 로그인 토큰 발급
        Optional<Member> linkedMember = memberService.findBySocialAccount(provider, profile.providerUserId());
        if (linkedMember.isPresent()) {
            issueLoginTokens(linkedMember.get(), response);
            return SocialCallbackResult.loginSuccess();
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
            return SocialCallbackResult.linkRequired(accountLinkToken);
        }

        // 4. 소셜 계정도 기존 회원도 없으면 신규 사용자
        // 정제 후 닉네임이 남는 글자가 하나도 없는 경우 (전부 이모지 or 특수문자였던 경우)
        if (!StringUtils.hasText(profile.nickname())) {
            throw new AuthException(AuthErrorCode.SOCIAL_NICKNAME_NOT_FOUND);
        }

        // 회원가입에 필요한 데이터에 접근하는 토큰 문자열 키
        String socialSignupToken = socialSignupTokenStore.issue(new PendingSocialSignup(
                provider, profile.providerUserId(), profile.email(), profile.nickname()));
        return SocialCallbackResult.signupRequired(socialSignupToken);
    }

    /**
     * 소셜 회원가입 요청 시 실행 ([가입 완료] 버튼 누를 시)
     * 이미 콜백될 때 이메일/닉네임 확인이 완료된 상태
     * socialSignupToken에 연결된 회원가입 데이터로 신규 회원과 약관 동의 저장을 한 트랜잭션으로 처리
     * 회원가입과 동시에 로그인 처리 (자동 로그인)
     */
    @Transactional
    public AuthResponse.SocialSignUp signUp(SocialProvider provider, AuthRequest.SocialSignUp request,
                                            HttpServletResponse response) {

        // 1. 약관 동의 검증 (토큰 소비보다 우선 위치로 두어 약관 문제 발생 시 수정을 가능하게 함)
        policyService.validateAgreements(request.policyAgreements());

        // 2. 콜백이 발급해둔 회원가입 필요 데이터를 꺼내며 즉시 폐기 (1회용)
        PendingSocialSignup pending = socialSignupTokenStore.consume(request.socialSignupToken())
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_LINK_TOKEN_INVALID));

        // 3. URL provider와 토큰에 담긴 provider가 다르면 조작으로 간주해 차단
        if (pending.provider() != provider) {
            throw new AuthException(AuthErrorCode.ACCOUNT_LINK_TOKEN_INVALID);
        }

        // 4. 토큰 발급~가입 완료 사이 동일한 소셜 계정으로 이미 가입됐으면 중복 가입 차단 (동시 요청 방어)
        if (memberService.isSocialAccountLinked(provider, pending.providerUserId())) {
            throw new AuthException(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS);
        }

        // 5. 콜백 시점엔 신규 사용자로 판단됐지만, 약관 동의 화면에서 같은 이메일로 먼저 가입이 완료됐는지 확인
        // 중복 이메일로 가입 시 DB 유니크 제약 위반 (COMMON500_1)
        if (memberService.findByEmail(pending.email()).isPresent()) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 6. 회원 생성 + 소셜 계정 저장 후 약관 동의 이력 저장 (이메일 중복 검사 + 소셜 계정 중복 검사 완료한 상태)
        Member member = memberService.createSocialMember(
                pending.email(), pending.nickname(), provider, pending.providerUserId());
        policyService.agreeToPolicies(member, request.policyAgreements());

        // 7. 가입과 동시에 로그인 처리 - 신규 세션(기기) 발급 후 토큰 발급
        String role = member.getRole().name();
        String sessionId = jwtProvider.generateSessionId();
        String accessToken = jwtProvider.createAccessToken(member.getId(), role, sessionId);
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), role, sessionId);
        refreshTokenStore.save(member.getId(), sessionId, refreshToken, jwtProvider.getRefreshTokenValidity());
        cookieUtil.setRefreshTokenCookie(response, refreshToken, jwtProvider.getRefreshTokenValidity());

        return AuthConverter.toSocialSignUpResponse(member, provider, s3Service::getPublicUrl,
                accessToken, jwtProvider.getAccessTokenExpiresIn());
    }

    /**
     * 소셜 계정 연동 요청 시 실행
     * 임시 토큰의 기존 회원에 소셜 계정을 붙이고 로그인 토큰을 발급
     */
    @Transactional
    public AuthResponse.SocialLink link(SocialProvider provider, AuthRequest.SocialLink request,
                                        HttpServletResponse response) {

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
        cookieUtil.setRefreshTokenCookie(response, refreshToken, jwtProvider.getRefreshTokenValidity());

        return AuthConverter.toSocialLinkResponse(member, socialAccount, accessToken,
                jwtProvider.getAccessTokenExpiresIn());
    }

    /**
     * 로그인 토큰 발급 (콜백에서 기존 회원 존재 확인 시 실행)
     * 콜백은 302 리다이렉트 전용 - 액세스 토큰은 포함 X
     * 프론트는 콜백 리다이렉트 후 따로 POST /auth/reissue API를 통해 accessToken 발급 받
     */
    private void issueLoginTokens(Member member, HttpServletResponse response) {
        String role = member.getRole().name();
        String sessionId = jwtProvider.generateSessionId();
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), role, sessionId);
        refreshTokenStore.save(member.getId(), sessionId, refreshToken, jwtProvider.getRefreshTokenValidity());
        cookieUtil.setRefreshTokenCookie(response, refreshToken, jwtProvider.getRefreshTokenValidity());
    }
}
