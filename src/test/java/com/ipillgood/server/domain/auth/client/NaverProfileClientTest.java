package com.ipillgood.server.domain.auth.client;

import com.ipillgood.server.domain.auth.client.dto.SocialProfile;
import com.ipillgood.server.domain.auth.code.AuthErrorCode;
import com.ipillgood.server.domain.auth.exception.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 네이버를 실제로 호출하지 않고 응답 파싱과 예외 변환을 검증
 * 네이버는 실패도 200으로 내려주므로 resultcode 확인이 동작하는지 확인
 */
class NaverProfileClientTest {

    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";
    private static final String ACCESS_TOKEN = "naver-access-token";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    private NaverProfileClient client() {
        SocialProperties properties = new SocialProperties(
                null,
                new SocialProperties.Naver(USER_INFO_URI));
        return new NaverProfileClient(properties, restClientBuilder.build());
    }

    @Test
    @DisplayName("네이버 응답에서 고유 ID와 이메일을 추출한다")
    void fetch_returnsProfile() {
        server.expect(requestTo(USER_INFO_URI))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withSuccess("""
                        {
                          "resultcode": "00",
                          "message": "success",
                          "response": {
                            "id": "abc-def-ghi",
                            "nickname": "kim",
                            "email": "kim@example.com"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        SocialProfile profile = client().fetch(ACCESS_TOKEN);

        // 네이버는 ID를 문자열로 주므로 변환 없이 그대로 사용한다
        assertEquals("abc-def-ghi", profile.providerUserId());
        assertEquals("kim@example.com", profile.email());
        server.verify();
    }

    @Test
    @DisplayName("이메일 제공에 동의하지 않으면 이메일이 null로 전달된다")
    void fetch_returnsNullEmailWhenNotAgreed() {
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("""
                        {
                          "resultcode": "00",
                          "response": { "id": "abc-def-ghi" }
                        }
                        """, MediaType.APPLICATION_JSON));

        SocialProfile profile = client().fetch(ACCESS_TOKEN);

        assertEquals("abc-def-ghi", profile.providerUserId());
        assertNull(profile.email());
    }

    @Test
    @DisplayName("resultcode가 성공이 아니면 200 응답이어도 예외를 던진다")
    void fetch_throwsWhenResultCodeNotSuccess() {
        // 네이버는 인증 실패를 HTTP 200 + resultcode로 알리므로 상태 코드만 믿으면 안 된다
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("""
                        {
                          "resultcode": "024",
                          "message": "Authentication failed"
                        }
                        """, MediaType.APPLICATION_JSON));

        NaverProfileClient client = client();

        AuthException exception = assertThrows(AuthException.class, () -> client.fetch(ACCESS_TOKEN));
        assertEquals(AuthErrorCode.NAVER_AUTH_FAILED.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 네이버 로그인 실패 예외를 던진다")
    void fetch_throwsWhenTokenInvalid() {
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        NaverProfileClient client = client();

        AuthException exception = assertThrows(AuthException.class, () -> client.fetch(ACCESS_TOKEN));
        assertEquals(AuthErrorCode.NAVER_AUTH_FAILED.getCode(), exception.getCode().getCode());
    }
}
