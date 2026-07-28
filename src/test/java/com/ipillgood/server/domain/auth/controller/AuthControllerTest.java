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

    @Test
    @DisplayName("로그아웃 시 필터가 전달한 세션 식별자로 해당 기기의 세션만 삭제된다")
    void logout_realHttpFlow_removesOnlyThisSession() throws Exception {
        // 1. 실제 로그인 API 호출로 진짜 액세스 토큰 발급
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + USERNAME + "\",\"password\":\"" + RAW_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = mapper.readTree(loginResult.getResponse().getContentAsString()).get("result");
        String accessToken = result.get("accessToken").asText();
        String refreshToken = result.get("refreshToken").asText();

        Claims claims = jwtProvider.parseRefreshToken(refreshToken);
        String sessionId = jwtProvider.getSessionId(claims);

        // 로그인 직후엔 세션이 저장돼 있어야 함
        assertTrue(refreshTokenStore.find(memberId, sessionId).isPresent());

        // 2. 실제 로그아웃 API 호출 (JwtAuthFilter -> @RequestAttribute 경로를 실제로 태움)
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 3. 필터가 넘겨준 sessionId로 정확히 그 세션만 삭제됐는지 확인
        assertTrue(refreshTokenStore.find(memberId, sessionId).isEmpty());
    }
}
