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
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 카카오를 실제로 호출하지 않고 토큰 교환·응답 파싱·예외 변환을 검증
 * MockRestServiceServer가 정해둔 응답을 돌려주므로 네트워크/토큰 없이 실행 가능
 */
class KakaoProfileClientTest {

    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String AUTHORIZE_URI = "https://kauth.kakao.com/oauth/authorize";
    private static final String CLIENT_ID = "test-client-id";
    private static final String REDIRECT_URI = "https://example.com/api/v1/auth/kakao/callback";
    private static final String ACCESS_TOKEN = "kakao-access-token";
    private static final String AUTH_CODE = "kakao-auth-code";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    private KakaoProfileClient client() {
        return client(null);
    }

    // clientSecret 유무에 따른 요청 차이를 테스트하기 위해 지정 가능하게 분리
    private KakaoProfileClient client(String clientSecret) {
        SocialProperties properties = new SocialProperties(
                new SocialProperties.Kakao(USER_INFO_URI, AUTHORIZE_URI, TOKEN_URI, CLIENT_ID, clientSecret, REDIRECT_URI),
                null);
        return new KakaoProfileClient(properties, restClientBuilder.build());
    }

    @Test
    @DisplayName("카카오 응답에서 회원번호와 이메일을 추출한다")
    void fetch_returnsProfile() {
        // connected_at 처럼 우리가 쓰지 않는 필드가 섞여 있어도 파싱에 성공해야 통과
        server.expect(requestTo(USER_INFO_URI))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withSuccess("""
                        {
                          "id": 12345678,
                          "connected_at": "2026-07-22T00:00:00Z",
                          "kakao_account": {
                            "is_email_valid": true,
                            "is_email_verified": true,
                            "email": "kim@example.com"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        SocialProfile profile = client().fetch(ACCESS_TOKEN);

        assertEquals("12345678", profile.providerUserId());
        assertEquals("kim@example.com", profile.email());
        server.verify();
    }

    @Test
    @DisplayName("이메일 제공에 동의하지 않으면 이메일이 null로 전달된다")
    void fetch_returnsNullEmailWhenNotAgreed() {
        // 동의하지 않으면 kakao_account 자체가 응답에서 빠질 수 있음
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("{\"id\": 12345678}", MediaType.APPLICATION_JSON));

        SocialProfile profile = client().fetch(ACCESS_TOKEN);

        assertEquals("12345678", profile.providerUserId());
        assertNull(profile.email());
    }

    @Test
    @DisplayName("인증되지 않은 이메일은 신뢰하지 않고 null로 전달한다")
    void fetch_returnsNullEmailWhenNotVerified() {
        // 미인증 이메일을 신뢰하면 남의 계정에 소셜 계정을 연동할 수 있음
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("""
                        {
                          "id": 12345678,
                          "kakao_account": {
                            "is_email_valid": true,
                            "is_email_verified": false,
                            "email": "victim@example.com"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        SocialProfile profile = client().fetch(ACCESS_TOKEN);

        assertNull(profile.email());
    }

    @Test
    @DisplayName("유효하지 않은 이메일은 신뢰하지 않고 null로 전달한다")
    void fetch_returnsNullEmailWhenNotValid() {
        // 다른 계정에 사용되어 만료된 이메일
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("""
                        {
                          "id": 12345678,
                          "kakao_account": {
                            "is_email_valid": false,
                            "is_email_verified": true,
                            "email": "expired@example.com"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        SocialProfile profile = client().fetch(ACCESS_TOKEN);

        assertNull(profile.email());
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 카카오 로그인 실패 예외를 던진다")
    void fetch_throwsWhenTokenInvalid() {
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        KakaoProfileClient client = client();

        // RestClientException 대신 AuthException이 나가도록 ('로그인 실패')
        AuthException exception = assertThrows(AuthException.class, () -> client.fetch(ACCESS_TOKEN));
        assertEquals(AuthErrorCode.KAKAO_AUTH_FAILED.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("인가 코드를 access_token으로 교환할 때 필요한 파라미터를 전부 담아 요청한다")
    void fetchByCode_sendsExpectedFormParametersAndReturnsProfile() {
        server.expect(requestTo(TOKEN_URI))
                .andExpect(content().string(containsString("grant_type=authorization_code")))
                .andExpect(content().string(containsString("client_id=" + CLIENT_ID)))
                .andExpect(content().string(containsString("redirect_uri=")))
                .andExpect(content().string(containsString("code=" + AUTH_CODE)))
                .andRespond(withSuccess("{\"access_token\": \"" + ACCESS_TOKEN + "\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USER_INFO_URI))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withSuccess("""
                        {
                          "id": 12345678,
                          "kakao_account": {
                            "is_email_valid": true,
                            "is_email_verified": true,
                            "email": "kim@example.com"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        SocialProfile profile = client().fetchByCode(AUTH_CODE, "unused-state");

        assertEquals("12345678", profile.providerUserId());
        assertEquals("kim@example.com", profile.email());
        server.verify();
    }

    @Test
    @DisplayName("client_secret이 설정돼 있으면 토큰 교환 요청에 포함한다")
    void fetchByCode_includesClientSecretWhenConfigured() {
        String clientSecret = "test-client-secret";
        server.expect(requestTo(TOKEN_URI))
                .andExpect(content().string(containsString("client_secret=" + clientSecret)))
                .andRespond(withSuccess("{\"access_token\": \"" + ACCESS_TOKEN + "\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("{\"id\": 12345678}", MediaType.APPLICATION_JSON));

        client(clientSecret).fetchByCode(AUTH_CODE, "unused-state");

        server.verify();
    }

    @Test
    @DisplayName("client_secret이 설정돼 있지 않으면 토큰 교환 요청에서 생략한다")
    void fetchByCode_omitsClientSecretWhenNotConfigured() {
        server.expect(requestTo(TOKEN_URI))
                .andExpect(content().string(not(containsString("client_secret"))))
                .andRespond(withSuccess("{\"access_token\": \"" + ACCESS_TOKEN + "\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("{\"id\": 12345678}", MediaType.APPLICATION_JSON));

        client(null).fetchByCode(AUTH_CODE, "unused-state");

        server.verify();
    }

    @Test
    @DisplayName("인가 코드가 만료·위조됐으면 토큰 교환 단계에서 실패한다")
    void fetchByCode_throwsWhenCodeExchangeFails() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        KakaoProfileClient client = client();

        AuthException exception = assertThrows(AuthException.class,
                () -> client.fetchByCode(AUTH_CODE, "unused-state"));
        assertEquals(AuthErrorCode.KAKAO_AUTH_FAILED.getCode(), exception.getCode().getCode());
        server.verify();
    }

    @Test
    @DisplayName("토큰 교환 응답이 200이어도 access_token이 없으면 실패한다")
    void fetchByCode_throwsWhenAccessTokenMissingInResponse() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        KakaoProfileClient client = client();

        AuthException exception = assertThrows(AuthException.class,
                () -> client.fetchByCode(AUTH_CODE, "unused-state"));
        assertEquals(AuthErrorCode.KAKAO_AUTH_FAILED.getCode(), exception.getCode().getCode());
        server.verify();
    }

    @Test
    @DisplayName("토큰 교환은 성공했지만 사용자 정보 조회에 실패하면 카카오 로그인 실패 예외를 던진다")
    void fetchByCode_throwsWhenUserInfoFailsAfterSuccessfulExchange() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withSuccess("{\"access_token\": \"" + ACCESS_TOKEN + "\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        KakaoProfileClient client = client();

        AuthException exception = assertThrows(AuthException.class,
                () -> client.fetchByCode(AUTH_CODE, "unused-state"));
        assertEquals(AuthErrorCode.KAKAO_AUTH_FAILED.getCode(), exception.getCode().getCode());
        server.verify();
    }
}
