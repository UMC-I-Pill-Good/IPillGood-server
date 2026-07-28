package com.ipillgood.server.domain.auth.service;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.auth.exception.AuthException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTokenTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InMemoryRefreshTokenStore refreshTokenStore;

    @Autowired
    private JwtProvider jwtProvider;

    private static final String USERNAME = "tester1";
    private static final String RAW_PASSWORD = "password123";

    private Long memberId;

    @BeforeEach
    void setUp() {
        refreshTokenStore.clear();
        memberRepository.deleteAll();

        Member member = Member.builder()
                .nickname("테스터")
                .username(USERNAME)
                .email("tester1@test.com")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .build();
        memberId = memberRepository.save(member).getId();
    }

    // 리프레시 토큰 안의 세션(기기) 식별자를 꺼내 저장소 조회에 사용
    private String sessionIdOf(String refreshToken) {
        Claims claims = jwtProvider.parseRefreshToken(refreshToken);
        return jwtProvider.getSessionId(claims);
    }

    @Test
    @DisplayName("로그인 시 발급된 리프레시 토큰이 저장소에 보관된다")
    void login_savesRefreshToken() {
        AuthResponse.Login login = authService.login(new AuthRequest.Login(USERNAME, RAW_PASSWORD));

        assertEquals(login.refreshToken(),
                refreshTokenStore.find(memberId, sessionIdOf(login.refreshToken())).orElse(null));
    }

    @Test
    @DisplayName("재발급 시 리프레시 토큰이 회전되고, 회전된 이전 토큰은 재사용이 차단된다")
    void reissue_rotatesAndRejectsReuse() {
        AuthResponse.Login login = authService.login(new AuthRequest.Login(USERNAME, RAW_PASSWORD));
        String oldRefresh = login.refreshToken();
        String sessionId = sessionIdOf(oldRefresh);

        AuthResponse.Login reissued = authService.reissue(new AuthRequest.Reissue(oldRefresh));

        // 새 리프레시 토큰으로 회전되고 저장소에 반영됨 (세션ID는 그대로 사용)
        assertNotEquals(oldRefresh, reissued.refreshToken());
        assertEquals(sessionId, sessionIdOf(reissued.refreshToken()));
        assertEquals(reissued.refreshToken(), refreshTokenStore.find(memberId, sessionId).orElse(null));

        // 회전된 이전 토큰으로 재발급 시도 -> 차단 + 저장분 폐기
        assertThrows(AuthException.class,
                () -> authService.reissue(new AuthRequest.Reissue(oldRefresh)));
        assertTrue(refreshTokenStore.find(memberId, sessionId).isEmpty());
    }

    @Test
    @DisplayName("로그아웃 시 해당 기기(세션)의 리프레시 토큰만 삭제된다")
    void logout_deletesRefreshToken() {
        AuthResponse.Login login = authService.login(new AuthRequest.Login(USERNAME, RAW_PASSWORD));
        String sessionId = sessionIdOf(login.refreshToken());

        authService.logout(memberId, sessionId);

        assertTrue(refreshTokenStore.find(memberId, sessionId).isEmpty());
    }

    @Test
    @DisplayName("다중 기기 로그인 - 같은 회원이 두 번 로그인해도 두 세션 모두 독립적으로 유효하다")
    void multipleLogins_areIndependentSessions() {
        AuthResponse.Login deviceA = authService.login(new AuthRequest.Login(USERNAME, RAW_PASSWORD));
        AuthResponse.Login deviceB = authService.login(new AuthRequest.Login(USERNAME, RAW_PASSWORD));

        String sessionA = sessionIdOf(deviceA.refreshToken());
        String sessionB = sessionIdOf(deviceB.refreshToken());

        assertNotEquals(sessionA, sessionB);
        assertEquals(deviceA.refreshToken(), refreshTokenStore.find(memberId, sessionA).orElse(null));
        assertEquals(deviceB.refreshToken(), refreshTokenStore.find(memberId, sessionB).orElse(null));

        // A 기기만 로그아웃해도 B 기기는 그대로 유효
        authService.logout(memberId, sessionA);
        assertTrue(refreshTokenStore.find(memberId, sessionA).isEmpty());
        assertEquals(deviceB.refreshToken(), refreshTokenStore.find(memberId, sessionB).orElse(null));
    }
}
