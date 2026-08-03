package com.ipillgood.server.domain.auth.service;

import com.ipillgood.server.domain.auth.client.SocialProfileClient;
import com.ipillgood.server.domain.auth.client.SocialProfileClientResolver;
import com.ipillgood.server.domain.auth.client.dto.SocialProfile;
import com.ipillgood.server.domain.auth.code.AuthErrorCode;
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
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.member.repository.MemberSocialAccountRepository;
import com.ipillgood.server.domain.policy.dto.PolicyRequest;
import com.ipillgood.server.domain.policy.repository.MemberPolicyAgreementRepository;
import com.ipillgood.server.domain.policy.repository.PolicyDocumentRepository;
import com.ipillgood.server.global.security.jwt.CookieUtil;
import com.ipillgood.server.global.security.jwt.InMemoryRefreshTokenStore;
import com.ipillgood.server.global.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 소셜 로그인 콜백의 네 갈래 판정, 소셜 회원가입, 계정 연동이 올바르게 동작하는지 검증
 * 카카오를 실제로 호출하지 않고 가짜 클라이언트로 대체
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(SocialAuthServiceTest.FakeSocialClientConfig.class)
class SocialAuthServiceTest {

    private static final String PROVIDER_USER_ID = "kakao-12345678";
    private static final String EMAIL = "social@test.com";
    private static final String NICKNAME = "소셜닉네임";
    private static final String AUTH_CODE = "auth-code";
    private static final String STATE = "state-value";

    // 빈으로 등록하면 실제 리졸버가 KAKAO 구현체를 둘 받아 중복 키로 실패하므로, 스프링 밖에서 직접 들고 있는다
    private static final FakeSocialProfileClient FAKE_CLIENT = new FakeSocialProfileClient();

    @Autowired
    private SocialAuthService socialAuthService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberSocialAccountRepository memberSocialAccountRepository;

    @Autowired
    private AccountLinkTokenStore accountLinkTokenStore;

    @Autowired
    private SocialSignupTokenStore socialSignupTokenStore;

    @Autowired
    private InMemoryRefreshTokenStore refreshTokenStore;

    @Autowired
    private PolicyDocumentRepository policyDocumentRepository;

    @Autowired
    private MemberPolicyAgreementRepository memberPolicyAgreementRepository;

    @Autowired
    private JwtProvider jwtProvider;

    // 리프레시 토큰 안의 세션(기기) 식별자를 꺼내 저장소 조회에 사용
    private String sessionIdOf(String refreshToken) {
        Claims claims = jwtProvider.parseRefreshToken(refreshToken);
        return jwtProvider.getSessionId(claims);
    }

    @BeforeEach
    void setUp() {

        // 약관 이력·소셜 계정이 회원을 참조하므로 자식 -> 부모 순으로 정리
        memberPolicyAgreementRepository.deleteAll();
        memberSocialAccountRepository.deleteAll();
        memberRepository.deleteAll();
        refreshTokenStore.clear();

        FAKE_CLIENT.setProfile(new SocialProfile(PROVIDER_USER_ID, EMAIL, NICKNAME));
    }

    // 로컬 가입 회원 (아이디/비밀번호 보유, 소셜 연동 없음)
    private Member saveLocalMember() {
        return memberRepository.save(Member.builder()
                .nickname("테스터")
                .username("tester1")
                .email(EMAIL)
                .password("encoded-password")
                .build());
    }

    // 시더가 넣어둔 활성 약관 전체에 동의하는 회원가입 요청 (필수 약관이 모두 포함되어 검증을 통과)
    private AuthRequest.SocialSignUp signUpRequest(String socialSignupToken) {
        List<PolicyRequest.Agreement> agreements = policyDocumentRepository.findByActiveTrue().stream()
                .map(document -> new PolicyRequest.Agreement(document.getId(), true))
                .toList();
        return new AuthRequest.SocialSignUp(socialSignupToken, agreements);
    }

    // 콜백이 완전 신규로 판정하며 회원가입 대기 정보를 저장한 상태를 재현 (handleCallback을 거치지 않고 직접 시딩)
    private String issueSignupToken(SocialProvider provider) {
        return socialSignupTokenStore.issue(new PendingSocialSignup(provider, PROVIDER_USER_ID, EMAIL, NICKNAME));
    }

    // 연동 대기 정보를 저장하고 임시 토큰을 발급 (연동 요청 직전 상태 재현)
    private String issueLinkToken(Member member, SocialProvider provider) {
        return accountLinkTokenStore.issue(new PendingSocialLink(
                member.getId(), provider, PROVIDER_USER_ID, EMAIL));
    }

    @Test
    @DisplayName("이미 연동된 소셜 계정이면 로그인 성공으로 판정하고 refreshToken 쿠키를 발급한다")
    void handleCallback_returnsLoginSuccessWhenSocialAccountLinked() {
        Member member = saveLocalMember();
        memberSocialAccountRepository.save(
                MemberSocialAccount.of(member, SocialProvider.KAKAO, PROVIDER_USER_ID, EMAIL));

        MockHttpServletResponse httpResponse = new MockHttpServletResponse();
        SocialCallbackResult result = socialAuthService.handleCallback(
                SocialProvider.KAKAO, AUTH_CODE, STATE, httpResponse);

        assertEquals(SocialCallbackResult.Status.LOGIN_SUCCESS, result.status());

        // 콜백은 accessToken을 응답에 담지 않고 refreshToken 쿠키만 발급한다
        String refreshToken = httpResponse.getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME).getValue();
        assertEquals(refreshToken,
                refreshTokenStore.find(member.getId(), sessionIdOf(refreshToken)).orElse(null));
    }

    @Test
    @DisplayName("이메일을 확인할 수 없으면 기존 회원 판단이 불가능하므로 실패한다")
    void handleCallback_throwsWhenEmailMissing() {
        // 이메일 제공에 동의하지 않으면 가입도 연동도 할 수 없다
        FAKE_CLIENT.setProfile(new SocialProfile(PROVIDER_USER_ID, null, NICKNAME));

        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.handleCallback(
                        SocialProvider.KAKAO, AUTH_CODE, STATE, new MockHttpServletResponse()));

        assertEquals(AuthErrorCode.SOCIAL_EMAIL_NOT_FOUND.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("같은 이메일의 기존 회원이 있으면 연동 대기 정보를 담은 임시 토큰을 발급한다")
    void handleCallback_returnsLinkRequiredWhenEmailMatches() {
        Member member = saveLocalMember();

        SocialCallbackResult result = socialAuthService.handleCallback(
                SocialProvider.KAKAO, AUTH_CODE, STATE, new MockHttpServletResponse());

        assertEquals(SocialCallbackResult.Status.LINK_REQUIRED, result.status());
        assertNotNull(result.accountLinkToken());

        PendingSocialLink pending = accountLinkTokenStore.consume(result.accountLinkToken()).orElseThrow();
        assertEquals(member.getId(), pending.memberId());
        assertEquals(SocialProvider.KAKAO, pending.provider());
        assertEquals(PROVIDER_USER_ID, pending.providerUserId());
    }

    @Test
    @DisplayName("소셜 계정도 같은 이메일 회원도 없으면 회원가입 대기 정보를 담은 임시 토큰을 발급한다")
    void handleCallback_returnsSignupRequiredForNewUser() {
        SocialCallbackResult result = socialAuthService.handleCallback(
                SocialProvider.KAKAO, AUTH_CODE, STATE, new MockHttpServletResponse());

        assertEquals(SocialCallbackResult.Status.SIGNUP_REQUIRED, result.status());
        assertNotNull(result.socialSignupToken());

        PendingSocialSignup pending = socialSignupTokenStore.consume(result.socialSignupToken()).orElseThrow();
        assertEquals(SocialProvider.KAKAO, pending.provider());
        assertEquals(PROVIDER_USER_ID, pending.providerUserId());
        assertEquals(EMAIL, pending.email());
        assertEquals(NICKNAME, pending.nickname());
    }

    @Test
    @DisplayName("완전 신규 사용자인데 닉네임을 확인할 수 없으면 실패한다")
    void handleCallback_throwsWhenNicknameMissingForNewUser() {
        FAKE_CLIENT.setProfile(new SocialProfile(PROVIDER_USER_ID, EMAIL, null));

        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.handleCallback(
                        SocialProvider.KAKAO, AUTH_CODE, STATE, new MockHttpServletResponse()));

        assertEquals(AuthErrorCode.SOCIAL_NICKNAME_NOT_FOUND.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("신규 소셜 사용자는 회원·소셜계정·약관 동의가 저장되고 가입과 동시에 로그인 토큰을 발급한다")
    void signUp_createsMemberAndIssuesLoginTokens() {
        String socialSignupToken = issueSignupToken(SocialProvider.KAKAO);

        MockHttpServletResponse httpResponse = new MockHttpServletResponse();
        AuthResponse.SocialSignUp response = socialAuthService.signUp(
                SocialProvider.KAKAO, signUpRequest(socialSignupToken), httpResponse);

        // 콜백이 미리 조회해둔 닉네임/이메일 그대로 응답에 포함
        assertNotNull(response.memberId());
        assertEquals(NICKNAME, response.nickname());
        assertEquals(EMAIL, response.email());
        assertEquals(SocialProvider.KAKAO, response.provider());

        // 회원·소셜계정·약관 이력 실제로 저장
        assertTrue(memberRepository.findByEmail(EMAIL).isPresent());
        assertTrue(memberSocialAccountRepository
                .existsByProviderAndProviderUserId(SocialProvider.KAKAO, PROVIDER_USER_ID));
        assertFalse(memberPolicyAgreementRepository.findAll().isEmpty());

        // 로컬 회원가입과 달리 소셜 회원가입은 가입과 동시에 로그인 처리된다
        assertNotNull(response.accessToken());
        assertEquals("Bearer", response.tokenType());
        String refreshToken = httpResponse.getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME).getValue();
        assertEquals(refreshToken,
                refreshTokenStore.find(response.memberId(), sessionIdOf(refreshToken)).orElse(null));
    }

    @Test
    @DisplayName("유효하지 않은 회원가입 토큰이면 회원가입에 실패한다")
    void signUp_throwsWhenTokenInvalid() {
        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.signUp(SocialProvider.KAKAO, signUpRequest("unknown-token"),
                        new MockHttpServletResponse()));

        assertEquals(AuthErrorCode.ACCOUNT_LINK_TOKEN_INVALID.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("URL 제공자와 토큰의 제공자가 다르면 회원가입에 실패한다")
    void signUp_throwsWhenProviderMismatch() {
        // 토큰은 KAKAO로 발급됐는데 URL은 NAVER로 요청 -> 불일치
        String socialSignupToken = issueSignupToken(SocialProvider.KAKAO);

        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.signUp(SocialProvider.NAVER, signUpRequest(socialSignupToken),
                        new MockHttpServletResponse()));

        assertEquals(AuthErrorCode.ACCOUNT_LINK_TOKEN_INVALID.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("토큰 발급~소비 사이 동일 소셜 계정이 이미 가입됐으면 회원가입에 실패한다")
    void signUp_throwsWhenSocialAccountAlreadyExists() {
        String socialSignupToken = issueSignupToken(SocialProvider.KAKAO);

        // 같은 provider/providerUserId로 이미 가입된 회원을 만듦 (동시 요청 방어 시나리오)
        Member existing = memberRepository.save(Member.builder()
                .nickname("기존회원").email("other@test.com").build());
        memberSocialAccountRepository.save(
                MemberSocialAccount.of(existing, SocialProvider.KAKAO, PROVIDER_USER_ID, "other@test.com"));

        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.signUp(SocialProvider.KAKAO, signUpRequest(socialSignupToken),
                        new MockHttpServletResponse()));

        assertEquals(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("임시 토큰으로 기존 회원에 소셜 계정을 연동하고 로그인 토큰을 발급한다")
    void link_linksAccountAndIssuesTokens() {
        Member member = saveLocalMember();
        String token = issueLinkToken(member, SocialProvider.KAKAO);

        MockHttpServletResponse httpResponse = new MockHttpServletResponse();
        AuthResponse.SocialLink response = socialAuthService.link(
                SocialProvider.KAKAO, new AuthRequest.SocialLink(token), httpResponse);

        // 연동 결과 + 로그인 토큰이 함께 응답된다
        assertTrue(response.linked());
        assertEquals(SocialProvider.KAKAO, response.provider());
        assertNotNull(response.linkedAt());
        assertNotNull(response.accessToken());
        assertEquals(member.getId(), response.memberId());

        // 소셜 계정이 실제로 연동되고, 리프레시 토큰이 쿠키로 발급되고 저장소에 저장된다
        assertTrue(memberSocialAccountRepository
                .existsByProviderAndProviderUserId(SocialProvider.KAKAO, PROVIDER_USER_ID));
        String refreshToken = httpResponse.getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME).getValue();
        assertEquals(refreshToken,
                refreshTokenStore.find(member.getId(), sessionIdOf(refreshToken)).orElse(null));
    }

    @Test
    @DisplayName("유효하지 않은 임시 토큰이면 연동에 실패한다")
    void link_throwsWhenTokenInvalid() {
        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.link(SocialProvider.KAKAO, new AuthRequest.SocialLink("unknown-token"),
                        new MockHttpServletResponse()));

        assertEquals(AuthErrorCode.ACCOUNT_LINK_TOKEN_INVALID.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("URL 제공자와 토큰의 제공자가 다르면 연동에 실패한다")
    void link_throwsWhenProviderMismatch() {
        Member member = saveLocalMember();
        String token = issueLinkToken(member, SocialProvider.KAKAO);

        // 토큰은 KAKAO인데 URL은 NAVER로 요청 -> 불일치
        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.link(SocialProvider.NAVER, new AuthRequest.SocialLink(token),
                        new MockHttpServletResponse()));

        assertEquals(AuthErrorCode.ACCOUNT_LINK_TOKEN_INVALID.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("이미 연동된 소셜 계정이면 연동에 실패한다")
    void link_throwsWhenSocialAccountAlreadyExists() {
        Member member = saveLocalMember();
        memberSocialAccountRepository.save(
                MemberSocialAccount.of(member, SocialProvider.KAKAO, PROVIDER_USER_ID, EMAIL));
        String token = issueLinkToken(member, SocialProvider.KAKAO);

        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.link(SocialProvider.KAKAO, new AuthRequest.SocialLink(token),
                        new MockHttpServletResponse()));

        assertEquals(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS.getCode(), exception.getCode().getCode());
    }

    /**
     * 실제 카카오/네이버 클라이언트 대신 지정한 프로필을 그대로 돌려주는 리졸버로 교체
     * 실제 클라이언트 빈도 함께 등록되어 있으므로, @Primary로 주입 우선권을 가져간다
     */
    @TestConfiguration
    static class FakeSocialClientConfig {

        @Bean
        @Primary
        SocialProfileClientResolver fakeSocialProfileClientResolver() {
            return new SocialProfileClientResolver(List.of(FAKE_CLIENT));
        }
    }

    /**
     * 테스트가 지정한 SocialProfile을 그대로 반환하는 가짜 클라이언트
     * 이메일/닉네임 미동의처럼 실제 소셜에서 재현하기 어려운 상황을 만들기 위해 사용
     */
    static class FakeSocialProfileClient implements SocialProfileClient {

        private SocialProfile profile;

        void setProfile(SocialProfile profile) {
            this.profile = profile;
        }

        @Override
        public SocialProvider provider() {
            return SocialProvider.KAKAO;
        }

        @Override
        public SocialProfile fetch(String providerAccessToken) {
            return profile;
        }

        @Override
        public SocialProfile fetchByCode(String code, String state) {
            return profile;
        }
    }
}
