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
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.MemberSocialAccount;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.member.repository.MemberSocialAccountRepository;
import com.ipillgood.server.domain.policy.dto.PolicyRequest;
import com.ipillgood.server.domain.policy.repository.MemberPolicyAgreementRepository;
import com.ipillgood.server.domain.policy.repository.PolicyDocumentRepository;
import com.ipillgood.server.global.security.jwt.InMemoryRefreshTokenStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 소셜 로그인의 네 갈래 판정과 소셜 회원가입의 분기가 올바르게 동작하는지 검증
 * 카카오를 실제로 호출하지 않고 가짜 클라이언트로 대체
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(SocialAuthServiceTest.FakeSocialClientConfig.class)
class SocialAuthServiceTest {

    private static final String PROVIDER_USER_ID = "kakao-12345678";
    private static final String EMAIL = "social@test.com";
    private static final String NICKNAME = "소셜닉네임";
    private static final AuthRequest.SocialLogin REQUEST = new AuthRequest.SocialLogin("access-token");

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
    private InMemoryRefreshTokenStore refreshTokenStore;

    @Autowired
    private PolicyDocumentRepository policyDocumentRepository;

    @Autowired
    private MemberPolicyAgreementRepository memberPolicyAgreementRepository;

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
    private AuthRequest.SocialSignUp signUpRequest() {
        List<PolicyRequest.Agreement> agreements = policyDocumentRepository.findByActiveTrue().stream()
                .map(document -> new PolicyRequest.Agreement(document.getId(), true))
                .toList();
        return new AuthRequest.SocialSignUp("access-token", agreements);
    }

    // 연동 대기 정보를 저장하고 임시 토큰을 발급 (연동 요청 직전 상태 재현)
    private String issueLinkToken(Member member, SocialProvider provider) {
        return accountLinkTokenStore.issue(new PendingSocialLink(
                member.getId(), provider, PROVIDER_USER_ID, EMAIL));
    }

    @Test
    @DisplayName("이미 연동된 소셜 계정이면 로그인 토큰을 발급한다")
    void login_returnsTokensWhenSocialAccountLinked() {
        Member member = saveLocalMember();
        memberSocialAccountRepository.save(
                MemberSocialAccount.of(member, SocialProvider.KAKAO, PROVIDER_USER_ID, EMAIL));

        AuthResponse.SocialLogin response = socialAuthService.login(SocialProvider.KAKAO, REQUEST);

        assertFalse(response.signupRequired());
        assertFalse(response.accountLinkRequired());
        assertNotNull(response.accessToken());
        assertEquals(member.getId(), response.memberId());
        assertEquals("Bearer", response.tokenType());

        // 재발급 검증에 쓰이도록 리프레시 토큰이 저장소에 보관되어야 한다
        assertEquals(response.refreshToken(), refreshTokenStore.find(member.getId()).orElse(null));
    }

    @Test
    @DisplayName("이메일을 확인할 수 없으면 기존 회원 판단이 불가능하므로 실패한다")
    void login_throwsWhenEmailMissing() {
        // 이메일 제공에 동의하지 않으면 가입도 연동도 할 수 없다
        FAKE_CLIENT.setProfile(new SocialProfile(PROVIDER_USER_ID, null, NICKNAME));

        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.login(SocialProvider.KAKAO, REQUEST));

        assertEquals(AuthErrorCode.SOCIAL_EMAIL_NOT_FOUND.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("같은 이메일의 기존 회원이 있으면 연동 대기 정보를 담은 임시 토큰을 발급한다")
    void login_returnsAccountLinkTokenWhenEmailMatches() {
        Member member = saveLocalMember();

        AuthResponse.SocialLogin response = socialAuthService.login(SocialProvider.KAKAO, REQUEST);

        assertFalse(response.signupRequired());
        assertTrue(response.accountLinkRequired());
        assertNotNull(response.accountLinkToken());

        // 아직 사용자가 동의하기 전이므로 로그인 토큰은 발급되지 않아야 한다
        assertNull(response.accessToken());
        assertNull(response.refreshToken());

        // 회원 ID는 응답에 싣지 않고 서버가 저장소에 보관한다
        assertNull(response.memberId());
        PendingSocialLink pending = accountLinkTokenStore.consume(response.accountLinkToken()).orElseThrow();
        assertEquals(member.getId(), pending.memberId());
        assertEquals(SocialProvider.KAKAO, pending.provider());
        assertEquals(PROVIDER_USER_ID, pending.providerUserId());
    }

    @Test
    @DisplayName("소셜 계정도 같은 이메일 회원도 없으면 회원가입이 필요하다고 응답한다")
    void login_returnsSignUpRequiredForNewUser() {
        AuthResponse.SocialLogin response = socialAuthService.login(SocialProvider.KAKAO, REQUEST);

        assertTrue(response.signupRequired());
        assertFalse(response.accountLinkRequired());
        assertNull(response.accountLinkToken());
        assertNull(response.accessToken());
    }

    @Test
    @DisplayName("신규 소셜 사용자는 회원·소셜계정·약관 동의가 저장되고 토큰 없이 응답한다")
    void signUp_createsMemberWithoutToken() {
        AuthResponse.SocialSignUp response = socialAuthService.signUp(SocialProvider.KAKAO, signUpRequest());

        // 소셜에서 가져온 닉네임/이메일 그대로 응답에 포함
        assertNotNull(response.memberId());
        assertEquals(NICKNAME, response.nickname());
        assertEquals(EMAIL, response.email());
        assertEquals(SocialProvider.KAKAO, response.provider());

        // 회원·소셜계정·약관 이력 실제로 저장
        assertTrue(memberRepository.findByEmail(EMAIL).isPresent());
        assertTrue(memberSocialAccountRepository
                .existsByProviderAndProviderUserId(SocialProvider.KAKAO, PROVIDER_USER_ID));
        assertFalse(memberPolicyAgreementRepository.findAll().isEmpty());

        // 소셜 회원가입은 자동 로그인하지 않으므로 리프레시 토큰이 저장 x
        assertTrue(refreshTokenStore.find(response.memberId()).isEmpty());
    }

    @Test
    @DisplayName("이메일을 확인할 수 없으면 회원가입에 실패한다")
    void signUp_throwsWhenEmailMissing() {
        FAKE_CLIENT.setProfile(new SocialProfile(PROVIDER_USER_ID, null, NICKNAME));

        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.signUp(SocialProvider.KAKAO, signUpRequest()));

        assertEquals(AuthErrorCode.SOCIAL_EMAIL_NOT_FOUND.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("닉네임을 확인할 수 없으면 회원가입에 실패한다")
    void signUp_throwsWhenNicknameMissing() {
        FAKE_CLIENT.setProfile(new SocialProfile(PROVIDER_USER_ID, EMAIL, null));

        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.signUp(SocialProvider.KAKAO, signUpRequest()));

        assertEquals(AuthErrorCode.SOCIAL_NICKNAME_NOT_FOUND.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("이미 연동된 소셜 계정이면 회원가입에 실패한다")
    void signUp_throwsWhenSocialAccountAlreadyExists() {

        // 같은 provider/providerUserId로 이미 가입된 회원을 만듦
        Member existing = memberRepository.save(Member.builder()
                .nickname("기존회원").email("other@test.com").build());
        memberSocialAccountRepository.save(
                MemberSocialAccount.of(existing, SocialProvider.KAKAO, PROVIDER_USER_ID, "other@test.com"));

        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.signUp(SocialProvider.KAKAO, signUpRequest()));

        assertEquals(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 회원가입에 실패한다")
    void signUp_throwsWhenEmailAlreadyUsed() {

        // 같은 이메일의 회원이 이미 있으나 소셜 계정은 연동되지 않은 상태
        // 비정상적인 직접 호출 방어 경로. login을 건너뛰고 signup을 바로 호출하는 경우. (정상 로직: 연동)
        saveLocalMember();

        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.signUp(SocialProvider.KAKAO, signUpRequest()));

        assertEquals(AuthErrorCode.ACCOUNT_LINK_REQUIRED.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("임시 토큰으로 기존 회원에 소셜 계정을 연동하고 로그인 토큰을 발급한다")
    void link_linksAccountAndIssuesTokens() {
        Member member = saveLocalMember();
        String token = issueLinkToken(member, SocialProvider.KAKAO);

        AuthResponse.SocialLink response = socialAuthService.link(
                SocialProvider.KAKAO, new AuthRequest.SocialLink(token));

        // 연동 결과 + 로그인 토큰이 함께 응답된다
        assertTrue(response.linked());
        assertEquals(SocialProvider.KAKAO, response.provider());
        assertNotNull(response.linkedAt());
        assertNotNull(response.accessToken());
        assertEquals(member.getId(), response.memberId());

        // 소셜 계정이 실제로 연동되고, 리프레시 토큰이 저장된다
        assertTrue(memberSocialAccountRepository
                .existsByProviderAndProviderUserId(SocialProvider.KAKAO, PROVIDER_USER_ID));
        assertEquals(response.refreshToken(), refreshTokenStore.find(member.getId()).orElse(null));
    }

    @Test
    @DisplayName("유효하지 않은 임시 토큰이면 연동에 실패한다")
    void link_throwsWhenTokenInvalid() {
        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.link(SocialProvider.KAKAO, new AuthRequest.SocialLink("unknown-token")));

        assertEquals(AuthErrorCode.ACCOUNT_LINK_TOKEN_INVALID.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("URL 제공자와 토큰의 제공자가 다르면 연동에 실패한다")
    void link_throwsWhenProviderMismatch() {
        Member member = saveLocalMember();
        String token = issueLinkToken(member, SocialProvider.KAKAO);

        // 토큰은 KAKAO인데 URL은 NAVER로 요청 -> 불일치
        AuthException exception = assertThrows(AuthException.class,
                () -> socialAuthService.link(SocialProvider.NAVER, new AuthRequest.SocialLink(token)));

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
                () -> socialAuthService.link(SocialProvider.KAKAO, new AuthRequest.SocialLink(token)));

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
     * 이메일 미동의처럼 실제 소셜에서 재현하기 어려운 상황을 만들기 위해 사용
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
    }
}
