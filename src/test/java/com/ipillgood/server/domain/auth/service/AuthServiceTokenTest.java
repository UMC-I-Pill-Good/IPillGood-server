package com.ipillgood.server.domain.auth.service;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.auth.exception.AuthException;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.global.security.jwt.InMemoryRefreshTokenStore;
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

    @Test
    @DisplayName("로그인 시 발급된 리프레시 토큰이 저장소에 보관된다")
    void login_savesRefreshToken() {
        AuthResponse.Login login = authService.login(new AuthRequest.Login(USERNAME, RAW_PASSWORD));

        assertEquals(login.refreshToken(), refreshTokenStore.find(memberId).orElse(null));
    }

    @Test
    @DisplayName("재발급 시 리프레시 토큰이 회전되고, 회전된 이전 토큰은 재사용이 차단된다")
    void reissue_rotatesAndRejectsReuse() {
        AuthResponse.Login login = authService.login(new AuthRequest.Login(USERNAME, RAW_PASSWORD));
        String oldRefresh = login.refreshToken();

        AuthResponse.Login reissued = authService.reissue(new AuthRequest.Reissue(oldRefresh));

        // 새 리프레시 토큰으로 회전되고 저장소에 반영됨
        assertNotEquals(oldRefresh, reissued.refreshToken());
        assertEquals(reissued.refreshToken(), refreshTokenStore.find(memberId).orElse(null));

        // 회전된 이전 토큰으로 재발급 시도 -> 차단 + 저장분 폐기
        assertThrows(AuthException.class,
                () -> authService.reissue(new AuthRequest.Reissue(oldRefresh)));
        assertTrue(refreshTokenStore.find(memberId).isEmpty());
    }

    @Test
    @DisplayName("로그아웃 시 저장된 리프레시 토큰이 삭제된다")
    void logout_deletesRefreshToken() {
        authService.login(new AuthRequest.Login(USERNAME, RAW_PASSWORD));

        authService.logout(memberId);

        assertTrue(refreshTokenStore.find(memberId).isEmpty());
    }
}
