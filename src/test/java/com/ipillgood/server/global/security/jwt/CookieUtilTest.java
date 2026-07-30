package com.ipillgood.server.global.security.jwt;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieUtilTest {

    private CookieUtil cookieUtil;

    @BeforeEach
    void setUp() {
        cookieUtil = new CookieUtil();
        ReflectionTestUtils.setField(cookieUtil, "sameSite", "None");
    }

    @Test
    @DisplayName("refreshToken 쿠키를 httpOnly/secure로 설정하고 값과 만료 시간을 담는다")
    void setRefreshTokenCookie_setsExpectedAttributes() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieUtil.setRefreshTokenCookie(response, "sample-refresh-token", Duration.ofDays(14));

        Cookie cookie = response.getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME);
        assertEquals("sample-refresh-token", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals((int) Duration.ofDays(14).toSeconds(), cookie.getMaxAge());
    }

    @Test
    @DisplayName("쿠키 삭제 시 Max-Age를 0으로 설정해 브라우저가 즉시 만료시키게 한다")
    void clearRefreshTokenCookie_expiresImmediately() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieUtil.clearRefreshTokenCookie(response);

        Cookie cookie = response.getCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME);
        assertEquals(0, cookie.getMaxAge());
    }
}
