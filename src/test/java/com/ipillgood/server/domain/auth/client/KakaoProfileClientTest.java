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
 * 카카오를 실제로 호출하지 않고 응답 파싱과 예외 변환을 검증
 * MockRestServiceServer가 정해둔 응답을 돌려주므로 네트워크/토큰 없이 실행 가능
 */
class KakaoProfileClientTest {

    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
    private static final String TOKEN_INFO_URI = "https://kapi.kakao.com/v1/user/access_token_info";
    private static final String ACCESS_TOKEN = "kakao-access-token";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    // appId가 null이면 토큰 치환 검증을 건너뛴다
    private KakaoProfileClient client(String appId) {
        SocialProperties properties = new SocialProperties(
                new SocialProperties.Kakao(USER_INFO_URI, TOKEN_INFO_URI, appId),
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

        SocialProfile profile = client(null).fetch(ACCESS_TOKEN);

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

        SocialProfile profile = client(null).fetch(ACCESS_TOKEN);

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

        SocialProfile profile = client(null).fetch(ACCESS_TOKEN);

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

        SocialProfile profile = client(null).fetch(ACCESS_TOKEN);

        assertNull(profile.email());
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 카카오 로그인 실패 예외를 던진다")
    void fetch_throwsWhenTokenInvalid() {
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        KakaoProfileClient client = client(null);

        // RestClientException 대신 AuthException이 나가도록 ('로그인 실패')
        AuthException exception = assertThrows(AuthException.class, () -> client.fetch(ACCESS_TOKEN));
        assertEquals(AuthErrorCode.KAKAO_AUTH_FAILED.getCode(), exception.getCode().getCode());
    }

    // 토큰 치환 공격 방어
    @Test
    @DisplayName("앱 ID가 설정되면 다른 앱에서 발급된 토큰을 차단한다")
    void fetch_rejectsTokenFromAnotherApp() {
        server.expect(requestTo(TOKEN_INFO_URI))
                .andRespond(withSuccess("{\"id\": 12345678, \"app_id\": 99999}", MediaType.APPLICATION_JSON));

        KakaoProfileClient client = client("12345");

        assertThrows(AuthException.class, () -> client.fetch(ACCESS_TOKEN));

        // 앱 검증에서 막혔으므로 사용자 정보 조회 불가능
        server.verify();
    }

    @Test
    @DisplayName("앱 ID가 일치하면 사용자 정보 조회까지 진행한다")
    void fetch_acceptsTokenFromThisApp() {
        server.expect(requestTo(TOKEN_INFO_URI))
                .andRespond(withSuccess("{\"app_id\": 12345}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("""
                        {
                          "id": 777,
                          "kakao_account": {
                            "is_email_valid": true,
                            "is_email_verified": true,
                            "email": "kim@example.com"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        SocialProfile profile = client("12345").fetch(ACCESS_TOKEN);

        assertEquals("777", profile.providerUserId());
        assertEquals("kim@example.com", profile.email());
        server.verify();
    }
}
