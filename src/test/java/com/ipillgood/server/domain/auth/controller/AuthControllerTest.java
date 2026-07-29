package com.ipillgood.server.domain.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.global.security.jwt.InMemoryRefreshTokenStore;
import com.ipillgood.server.global.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JwtAuthFilter가 request attribute로 넘긴 sessionId를
 * AuthController.logout이 @RequestAttribute로 실제로 잘 받아 처리하는지 실제 HTTP 요청으로 검증
 */
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
    private String[] login() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + USERNAME + "\",\"password\":\"" + RAW_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode result = new ObjectMapper().readTree(loginResult.getResponse().getContentAsString()).get("result");
        return new String[]{result.get("accessToken").asText(), result.get("refreshToken").asText()};
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
}
