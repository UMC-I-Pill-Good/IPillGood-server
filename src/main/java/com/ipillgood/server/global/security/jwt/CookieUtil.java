package com.ipillgood.server.global.security.jwt;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * refreshToken + oauth_state httpOnly 쿠키 설정
 */
@Component
public class CookieUtil {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    // CSRF를 방어하기 위한 state 문자열 값
    public static final String OAUTH_STATE_COOKIE_NAME = "oauth_state";
    private static final String COOKIE_PATH = "/";

    // oauth_state는 카카오/네이버 -> 우리 서버로 리다이렉트만 진행하므로 (페이지만 이동 fetch X)
    // SameSite=Lax로 설정하여 리다이렉트만 쿠키를 보내주도록 설정
    private static final String OAUTH_STATE_SAME_SITE = "Lax";
    private static final Duration OAUTH_STATE_MAX_AGE = Duration.ofMinutes(5);

    // SameSite 설정값 (application.yml) - refreshToken 전용 (fetch이므로 'None')
    @Value("${app.cookie.same-site}")
    private String refreshTokenSameSite;

    // 로그인/재발급 refreshToken을 httpOnly 쿠키에 담음 (헤더로 전달)
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, Duration maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshTokenSameSite, maxAge).toString());
    }

    // 로그아웃 시 refreshToken 쿠키를 즉시 만료 후 제거
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie(REFRESH_TOKEN_COOKIE_NAME, "", refreshTokenSameSite, Duration.ZERO).toString());
    }

    // 소셜 로그인 리다이렉트 시작 시 [CSRF 방지용 문자열 state 값]을 쿠키에 담음
    public void setOauthStateCookie(HttpServletResponse response, String state) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie(OAUTH_STATE_COOKIE_NAME, state, OAUTH_STATE_SAME_SITE, OAUTH_STATE_MAX_AGE).toString());
    }

    // 콜백에서 state 검증을 마친 뒤 oauth_state 쿠키를 즉시 만료 후 제거 (1회용)
    public void clearOauthStateCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie(OAUTH_STATE_COOKIE_NAME, "", OAUTH_STATE_SAME_SITE, Duration.ZERO).toString());
    }

    private ResponseCookie buildCookie(String name, String value, String sameSite, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite(sameSite)
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
