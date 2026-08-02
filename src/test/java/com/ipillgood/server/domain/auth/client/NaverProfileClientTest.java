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

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 네이버를 실제로 호출하지 않고 토큰 교환·응답 파싱·예외 변환을 검증
 * 네이버는 실패도 200으로 내려주므로 resultcode 확인이 동작하는지 확인
 */
class NaverProfileClientTest {

    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";
    private static final String TOKEN_URI = "https://nid.naver.com/oauth2.0/token";
    private static final String AUTHORIZE_URI = "https://nid.naver.com/oauth2.0/authorize";
    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String REDIRECT_URI = "https://example.com/api/v1/auth/naver/callback";
    private static final String ACCESS_TOKEN = "naver-access-token";
    private static final String AUTH_CODE = "naver-auth-code";
    private static final String STATE = "sample-state";

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
                new SocialProperties.Naver(USER_INFO_URI, AUTHORIZE_URI, TOKEN_URI, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI));
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

    @Test
    @DisplayName("인가 코드와 state를 access_token으로 교환할 때 필요한 파라미터를 전부 담아 요청한다")
    void fetchByCode_sendsExpectedFormParametersAndReturnsProfile() {
        server.expect(requestTo(TOKEN_URI))
                .andExpect(content().string(containsString("grant_type=authorization_code")))
                .andExpect(content().string(containsString("client_id=" + CLIENT_ID)))
                .andExpect(content().string(containsString("client_secret=" + CLIENT_SECRET)))
                .andExpect(content().string(containsString("code=" + AUTH_CODE)))
                .andExpect(content().string(containsString("state=" + STATE)))
                .andRespond(withSuccess("{\"access_token\": \"" + ACCESS_TOKEN + "\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USER_INFO_URI))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withSuccess("""
                        {
                          "resultcode": "00",
                          "response": {
                            "id": "abc-def-ghi",
                            "email": "kim@example.com"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        SocialProfile profile = client().fetchByCode(AUTH_CODE, STATE);

        assertEquals("abc-def-ghi", profile.providerUserId());
        assertEquals("kim@example.com", profile.email());
        server.verify();
    }

    @Test
    @DisplayName("인가 코드가 만료·위조됐으면 토큰 교환 단계에서 실패한다")
    void fetchByCode_throwsWhenCodeExchangeFails() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        NaverProfileClient client = client();

        AuthException exception = assertThrows(AuthException.class,
                () -> client.fetchByCode(AUTH_CODE, STATE));
        assertEquals(AuthErrorCode.NAVER_AUTH_FAILED.getCode(), exception.getCode().getCode());
        server.verify();
    }

    @Test
    @DisplayName("토큰 교환 응답이 200이어도 access_token이 없으면 실패한다")
    void fetchByCode_throwsWhenAccessTokenMissingInResponse() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        NaverProfileClient client = client();

        AuthException exception = assertThrows(AuthException.class,
                () -> client.fetchByCode(AUTH_CODE, STATE));
        assertEquals(AuthErrorCode.NAVER_AUTH_FAILED.getCode(), exception.getCode().getCode());
        server.verify();
    }

    @Test
    @DisplayName("토큰 교환은 성공했지만 사용자 정보 조회에 실패하면 네이버 로그인 실패 예외를 던진다")
    void fetchByCode_throwsWhenUserInfoFailsAfterSuccessfulExchange() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withSuccess("{\"access_token\": \"" + ACCESS_TOKEN + "\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        NaverProfileClient client = client();

        AuthException exception = assertThrows(AuthException.class,
                () -> client.fetchByCode(AUTH_CODE, STATE));
        assertEquals(AuthErrorCode.NAVER_AUTH_FAILED.getCode(), exception.getCode().getCode());
        server.verify();
    }
}
