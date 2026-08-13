package com.ipillgood.server.domain.auth.controller;

import com.ipillgood.server.domain.auth.client.SocialProfileClient;
import com.ipillgood.server.domain.auth.client.SocialProfileClientResolver;
import com.ipillgood.server.domain.auth.client.dto.SocialProfile;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.MemberSocialAccount;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.member.repository.MemberSocialAccountRepository;
import com.ipillgood.server.global.security.jwt.CookieUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * login/callback이 항상 302로 응답하는지, state 검증·4갈래 판정별 리다이렉트 목적지·
 * 예외의 리다이렉트 변환(지역 @ExceptionHandler)이 실제 HTTP 레벨에서 동작하는지 검증
 * 카카오/네이버를 실제로 호출하지 않고 가짜 클라이언트로 대체
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SocialOAuthRedirectControllerTest.FakeSocialClientConfig.class)
class SocialOAuthRedirectControllerTest {

    private static final String PROVIDER_USER_ID = "kakao-98765";
    private static final String EMAIL = "callback@test.com";
    private static final String NICKNAME = "콜백닉네임";
    private static final String STATE = "test-state-value";

    // 빈으로 등록하면 실제 리졸버가 KAKAO 구현체를 둘 받아 중복 키로 실패하므로, 스프링 밖에서 직접 들고 있는다
    private static final FakeSocialProfileClient FAKE_CLIENT = new FakeSocialProfileClient();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberSocialAccountRepository memberSocialAccountRepository;

    @Value("${app.frontend.callback-url}")
    private String frontendCallbackUrl;

    @BeforeEach
    void setUp() {
        memberSocialAccountRepository.deleteAll();
        memberRepository.deleteAll();
        FAKE_CLIENT.setProfile(new SocialProfile(PROVIDER_USER_ID, EMAIL, NICKNAME));
    }

    private Member saveLocalMember() {
        return memberRepository.save(Member.builder()
                .nickname("테스터")
                .username("cbtester1")
                .email(EMAIL)
                .password("encoded-password")
                .build());
    }

    @Test
    @DisplayName("카카오 로그인은 항상 302이고, state를 쿠키로 심고 카카오 인가 URL로 리다이렉트한다")
    void kakaoLogin_redirectsToAuthorizeUrlWithStateCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/kakao/login"))
                .andExpect(status().isFound())
                .andExpect(cookie().exists(CookieUtil.OAUTH_STATE_COOKIE_NAME))
                .andExpect(cookie().httpOnly(CookieUtil.OAUTH_STATE_COOKIE_NAME, true))
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertNotNull(location);
        assertTrue(location.startsWith("https://kauth.kakao.com/oauth/authorize"));
        assertTrue(location.contains("response_type=code"));

        // 리다이렉트 URL에 실린 state와 쿠키에 심긴 state가 같아야 콜백에서 비교가 가능함
        String stateInCookie = result.getResponse().getCookie(CookieUtil.OAUTH_STATE_COOKIE_NAME).getValue();
        assertTrue(location.contains("state=" + stateInCookie));
    }

    @Test
    @DisplayName("네이버 로그인은 네이버 인가 URL로 리다이렉트한다")
    void naverLogin_redirectsToNaverAuthorizeUrl() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/naver/login"))
                .andExpect(status().isFound())
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertNotNull(location);
        assertTrue(location.startsWith("https://nid.naver.com/oauth2.0/authorize"));
    }

    @Test
    @DisplayName("이미 연동된 회원이면 콜백은 프론트 콜백 URL(쿼리 없음)로 리다이렉트하고 refreshToken을 쿠키로 발급한다")
    void kakaoCallback_redirectsToFrontendWhenLoginSuccess() throws Exception {
        Member member = saveLocalMember();
        memberSocialAccountRepository.save(
                MemberSocialAccount.of(member, SocialProvider.KAKAO, PROVIDER_USER_ID, EMAIL));

        MvcResult result = mockMvc.perform(get("/api/v1/auth/kakao/callback")
                        .param("code", "any-code")
                        .param("state", STATE)
                        .cookie(new Cookie(CookieUtil.OAUTH_STATE_COOKIE_NAME, STATE)))
                .andExpect(status().isFound())
                .andReturn();

        assertEquals(frontendCallbackUrl, result.getResponse().getRedirectedUrl());
        assertNotNull(result.getResponse().getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME));
    }

    @Test
    @DisplayName("같은 이메일의 기존 회원이 있으면 accountLinkToken과 provider 쿼리로 리다이렉트한다")
    void kakaoCallback_redirectsWithLinkTokenWhenEmailMatches() throws Exception {
        saveLocalMember();

        MvcResult result = mockMvc.perform(get("/api/v1/auth/kakao/callback")
                        .param("code", "any-code")
                        .param("state", STATE)
                        .cookie(new Cookie(CookieUtil.OAUTH_STATE_COOKIE_NAME, STATE)))
                .andExpect(status().isFound())
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertTrue(location.startsWith(frontendCallbackUrl));
        assertTrue(location.contains("accountLinkToken="));
        assertTrue(location.contains("provider=kakao"));

        // 로그인 성공 케이스와 달리 아직 계정 확정 전이므로 refreshToken은 발급되지 않아야 함
        assertNull(result.getResponse().getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME));
    }

    @Test
    @DisplayName("소셜 계정도 같은 이메일 회원도 없으면 socialSignupToken과 provider 쿼리로 리다이렉트한다")
    void kakaoCallback_redirectsWithSignupTokenForNewUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/kakao/callback")
                        .param("code", "any-code")
                        .param("state", STATE)
                        .cookie(new Cookie(CookieUtil.OAUTH_STATE_COOKIE_NAME, STATE)))
                .andExpect(status().isFound())
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertTrue(location.startsWith(frontendCallbackUrl));
        assertTrue(location.contains("socialSignupToken="));
        assertTrue(location.contains("provider=kakao"));
    }

    @Test
    @DisplayName("oauth_state 쿠키가 없으면 state 불일치로 판단해 error=AUTH400_13으로 리다이렉트한다")
    void kakaoCallback_redirectsWithStateMismatchError_whenCookieMissing() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/kakao/callback")
                        .param("code", "any-code")
                        .param("state", STATE))
                // oauth_state 쿠키를 아예 안 보냄
                .andExpect(status().isFound())
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertTrue(location.startsWith(frontendCallbackUrl));
        assertTrue(location.contains("error=AUTH400_13"));
    }

    @Test
    @DisplayName("쿼리의 state와 쿠키의 state가 다르면 error=AUTH400_13으로 리다이렉트한다")
    void kakaoCallback_redirectsWithStateMismatchError_whenValuesDiffer() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/kakao/callback")
                        .param("code", "any-code")
                        .param("state", STATE)
                        .cookie(new Cookie(CookieUtil.OAUTH_STATE_COOKIE_NAME, "different-state")))
                .andExpect(status().isFound())
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertTrue(location.contains("error=AUTH400_13"));
    }

    @Test
    @DisplayName("사용자가 동의 화면에서 취소하면(error 쿼리 존재) error=AUTH400_14로 리다이렉트한다")
    void kakaoCallback_redirectsWithCancelledError_whenErrorQueryPresent() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/kakao/callback")
                        .param("state", STATE)
                        .param("error", "access_denied")
                        .cookie(new Cookie(CookieUtil.OAUTH_STATE_COOKIE_NAME, STATE)))
                .andExpect(status().isFound())
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertTrue(location.contains("error=AUTH400_14"));
    }

    @Test
    @DisplayName("error도 code도 없으면 error=AUTH400_7로 리다이렉트하고 토큰 교환을 시도하지 않는다")
    void kakaoCallback_redirectsWithAuthFailedError_whenCodeMissing() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/kakao/callback")
                        .param("state", STATE)
                        // code 파라미터를 아예 안 보냄
                        .cookie(new Cookie(CookieUtil.OAUTH_STATE_COOKIE_NAME, STATE)))
                .andExpect(status().isFound())
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertTrue(location.contains("error=AUTH400_7"));
    }

    @Test
    @DisplayName("이메일을 확인할 수 없으면 error=AUTH400_10으로 리다이렉트한다")
    void kakaoCallback_redirectsWithEmailNotFoundError() throws Exception {
        FAKE_CLIENT.setProfile(new SocialProfile(PROVIDER_USER_ID, null, NICKNAME));

        MvcResult result = mockMvc.perform(get("/api/v1/auth/kakao/callback")
                        .param("code", "any-code")
                        .param("state", STATE)
                        .cookie(new Cookie(CookieUtil.OAUTH_STATE_COOKIE_NAME, STATE)))
                .andExpect(status().isFound())
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertTrue(location.contains("error=AUTH400_10"));
    }

    /**
     * 실제 카카오/네이버 클라이언트 대신 지정한 프로필을 그대로 돌려주는 리졸버로 교체
     */
    @TestConfiguration
    static class FakeSocialClientConfig {

        @Bean
        @Primary
        SocialProfileClientResolver fakeSocialProfileClientResolver() {
            return new SocialProfileClientResolver(List.of(FAKE_CLIENT));
        }
    }

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
