package com.ipillgood.server.domain.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.global.security.jwt.CookieUtil;
import com.ipillgood.server.global.security.jwt.InMemoryRefreshTokenStore;
import com.ipillgood.server.global.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InMemoryRefreshTokenStore refreshTokenStore;

    @Autowired
    private JwtProvider jwtProvider;

    private static final String USERNAME = "logouttst";
    private static final String RAW_PASSWORD = "password123";
    private Long memberId;

    @BeforeEach
    void setUp() {
        refreshTokenStore.clear();
        memberRepository.deleteAll();
        Member member = Member.builder()
                .nickname("로그아웃테스터")
                .username(USERNAME)
                .email("logouttst@test.com")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .build();
        memberId = memberRepository.save(member).getId();
    }

    // 실제 로그인 API를 호출해 [accessToken, refreshToken]을 반환
    // accessToken은 응답 바디, refreshToken은 Set-Cookie에서 꺼냄
    private String[] login() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + USERNAME + "\",\"password\":\"" + RAW_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode result = new ObjectMapper().readTree(loginResult.getResponse().getContentAsString()).get("result");
        String accessToken = result.get("accessToken").asText();
        String refreshToken = loginResult.getResponse().getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME).getValue();
        return new String[]{accessToken, refreshToken};
    }

    private String sessionIdOf(String refreshToken) {
        Claims claims = jwtProvider.parseRefreshToken(refreshToken);
        return jwtProvider.getSessionId(claims);
    }

    @Test
    @DisplayName("로그아웃 시 필터가 전달한 세션 식별자로 해당 기기의 세션만 삭제되고, 다른 기기의 세션은 보존된다")
    void logout_realHttpFlow_removesOnlyThisSession() throws Exception {
        // 1. 두 번 로그인해 서로 다른 기기(세션)를 재현
        String[] deviceA = login();
        String[] deviceB = login();

        String sessionIdA = sessionIdOf(deviceA[1]);
        String sessionIdB = sessionIdOf(deviceB[1]);

        // 로그인 직후엔 두 세션 다 저장돼 있어야 함
        assertTrue(refreshTokenStore.find(memberId, sessionIdA).isPresent());
        assertTrue(refreshTokenStore.find(memberId, sessionIdB).isPresent());

        // 2. A 기기의 액세스 토큰으로 로그아웃 (JwtAuthFilter -> @RequestAttribute 경로를 실제로 태움)
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + deviceA[0]))
                .andExpect(status().isOk());

        // 3. 필터가 넘겨준 sessionId로 A 세션만 삭제되고, B 세션은 그대로 남아있어야 함
        //    (세션이 하나뿐이었다면 deleteAll로 잘못 구현돼도 통과했을 검증)
        assertTrue(refreshTokenStore.find(memberId, sessionIdA).isEmpty());
        assertTrue(refreshTokenStore.find(memberId, sessionIdB).isPresent());
    }

    @Test
    @DisplayName("로그아웃 응답에는 refreshToken 쿠키를 즉시 만료시키는 Set-Cookie가 포함된다")
    void logout_clearsRefreshTokenCookie() throws Exception {
        String[] device = login();

        MvcResult logoutResult = mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + device[0]))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cleared = logoutResult.getResponse().getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME);
        assertNotNull(cleared, "로그아웃 응답에 refreshToken Set-Cookie가 있어야 함");
        assertEquals(0, cleared.getMaxAge());
    }
}
