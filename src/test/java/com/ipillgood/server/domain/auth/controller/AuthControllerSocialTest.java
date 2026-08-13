package com.ipillgood.server.domain.auth.controller;

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
import com.ipillgood.server.domain.policy.fixture.PolicyFixture;
import com.ipillgood.server.domain.policy.repository.MemberPolicyAgreementRepository;
import com.ipillgood.server.domain.policy.repository.PolicyDocumentRepository;
import com.ipillgood.server.global.security.jwt.CookieUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController의 소셜 회원가입/계정 연동(카카오/네이버) 엔드포인트를 실제 HTTP 레벨에서 검증
 * 이 두 API는 소셜 제공자를 다시 호출하지 않고 토큰 저장소만 쓰므로 가짜 클라이언트가 필요 없음
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerSocialTest {

    private static final String PROVIDER_USER_ID = "kakao-55555";
    private static final String EMAIL = "controller-signup@test.com";
    private static final String NICKNAME = "컨트롤러닉네임";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberSocialAccountRepository memberSocialAccountRepository;

    @Autowired
    private PolicyDocumentRepository policyDocumentRepository;

    @Autowired
    private MemberPolicyAgreementRepository memberPolicyAgreementRepository;

    @Autowired
    private SocialSignupTokenStore socialSignupTokenStore;

    @Autowired
    private AccountLinkTokenStore accountLinkTokenStore;

    @BeforeEach
    void setUp() {
        memberPolicyAgreementRepository.deleteAll();
        memberSocialAccountRepository.deleteAll();
        memberRepository.deleteAll();

        policyDocumentRepository.deleteAll();
        policyDocumentRepository.saveAll(PolicyFixture.defaultDocuments());
    }

    private Member saveLocalMember() {
        return memberRepository.save(Member.builder()
                .nickname("테스터")
                .username("actester1")
                .email(EMAIL)
                .password("encoded-password")
                .build());
    }

    // 활성 약관 전체에 동의하는 요청 본문 (필수 약관이 모두 포함되어 검증을 통과)
    private String signUpRequestBody(String socialSignupToken) throws Exception {
        List<PolicyRequest.Agreement> agreements = policyDocumentRepository.findByActiveTrue().stream()
                .map(document -> new PolicyRequest.Agreement(document.getId(), true))
                .toList();

        String agreementsJson = agreements.stream()
                .map(a -> "{\"policyDocumentId\":" + a.policyDocumentId() + ",\"agreed\":" + a.agreed() + "}")
                .collect(Collectors.joining(",", "[", "]"));

        return "{\"socialSignupToken\":\"" + socialSignupToken + "\",\"policyAgreements\":" + agreementsJson + "}";
    }

    @Test
    @DisplayName("유효한 socialSignupToken으로 카카오 회원가입 시 201과 함께 로그인 토큰을 발급한다")
    void kakaoSignUp_createsMemberAndReturnsTokens() throws Exception {
        String token = socialSignupTokenStore.issue(
                new PendingSocialSignup(SocialProvider.KAKAO, PROVIDER_USER_ID, EMAIL, NICKNAME));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/kakao/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpRequestBody(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("AUTH201_2"))
                .andExpect(jsonPath("$.result.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.result.email").value(EMAIL))
                .andExpect(jsonPath("$.result.nickname").value(NICKNAME))
                .andReturn();

        assertNotNull(result.getResponse().getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME));
        assertTrue(memberSocialAccountRepository
                .existsByProviderAndProviderUserId(SocialProvider.KAKAO, PROVIDER_USER_ID));
    }

    @Test
    @DisplayName("socialSignupToken이 비어있으면 400을 반환한다")
    void kakaoSignUp_returns400WhenTokenBlank() throws Exception {
        mockMvc.perform(post("/api/v1/auth/kakao/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"socialSignupToken\":\"\",\"policyAgreements\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효하지 않은 socialSignupToken이면 401(AUTH401_3)을 반환한다")
    void kakaoSignUp_returns401WhenTokenInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/kakao/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpRequestBody("unknown-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_3"));
    }

    @Test
    @DisplayName("카카오 콜백이 발급한 토큰으로 네이버 가입을 시도하면 provider 불일치로 401을 반환한다")
    void naverSignUp_returns401WhenProviderMismatch() throws Exception {
        String token = socialSignupTokenStore.issue(
                new PendingSocialSignup(SocialProvider.KAKAO, PROVIDER_USER_ID, EMAIL, NICKNAME));

        mockMvc.perform(post("/api/v1/auth/naver/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpRequestBody(token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_3"));
    }

    @Test
    @DisplayName("유효한 accountLinkToken으로 카카오 계정 연동 시 200과 함께 로그인 토큰을 발급한다")
    void kakaoLink_linksAccountAndReturnsTokens() throws Exception {
        Member member = saveLocalMember();
        String token = accountLinkTokenStore.issue(
                new PendingSocialLink(member.getId(), SocialProvider.KAKAO, PROVIDER_USER_ID, EMAIL));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/kakao/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountLinkToken\":\"" + token + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("AUTH200_7"))
                .andExpect(jsonPath("$.result.linked").value(true))
                .andExpect(jsonPath("$.result.accessToken").isNotEmpty())
                .andReturn();

        assertNotNull(result.getResponse().getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME));
        assertTrue(memberSocialAccountRepository
                .existsByProviderAndProviderUserId(SocialProvider.KAKAO, PROVIDER_USER_ID));
    }

    @Test
    @DisplayName("accountLinkToken이 비어있으면 400을 반환한다")
    void kakaoLink_returns400WhenTokenBlank() throws Exception {
        mockMvc.perform(post("/api/v1/auth/kakao/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountLinkToken\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 연동된 소셜 계정으로 다시 연동을 시도하면 409(AUTH409_2)를 반환한다")
    void kakaoLink_returns409WhenAlreadyLinked() throws Exception {
        Member member = saveLocalMember();
        memberSocialAccountRepository.save(
                MemberSocialAccount.of(member, SocialProvider.KAKAO, PROVIDER_USER_ID, EMAIL));
        String token = accountLinkTokenStore.issue(
                new PendingSocialLink(member.getId(), SocialProvider.KAKAO, PROVIDER_USER_ID, EMAIL));

        mockMvc.perform(post("/api/v1/auth/kakao/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountLinkToken\":\"" + token + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH409_2"));
    }
}
