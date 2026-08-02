package com.ipillgood.server.domain.auth.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ipillgood.server.domain.auth.client.dto.SocialProfile;
import com.ipillgood.server.domain.auth.code.AuthErrorCode;
import com.ipillgood.server.domain.auth.exception.AuthException;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 네이버 사용자 정보 조회 클라이언트 (우리 서버 -> 네이버 API)
 * 응답에서 고유 ID와 이메일만 뽑아 공통 포맷(SocialProfile)으로 변환함
 */
@Component
public class NaverProfileClient implements SocialProfileClient {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String GRANT_TYPE = "authorization_code";

    // 네이버는 조회에 실패해도 200으로 응답하고 본문의 resultcode로 성패를 알린다. (성공: "00")
    private static final String RESULT_CODE_SUCCESS = "00";

    private final SocialProperties.Naver properties;
    private final RestClient restClient;

    public NaverProfileClient(SocialProperties socialProperties, RestClient socialRestClient) {
        this.properties = socialProperties.naver();
        this.restClient = socialRestClient;
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.NAVER;
    }

    @Override
    @Deprecated
    public SocialProfile fetch(String providerAccessToken) {

        // 1. 액세스 토큰으로 네이버에 사용자 정보 요청
        NaverUserResponse response;
        try {
            response = restClient.get()
                    .uri(properties.userInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + providerAccessToken)
                    .retrieve()
                    .body(NaverUserResponse.class);
        } catch (RestClientException e) {

            // 토큰이 만료·위조된 경우 네이버가 4xx로 응답 + RestClient가 예외 처리
            throw new AuthException(AuthErrorCode.NAVER_AUTH_FAILED);
        }

        // 2. 응답 검증
        // 네이버는 실패도 200으로 내려주므로 resultcode까지 확인
        if (response == null
                || !RESULT_CODE_SUCCESS.equals(response.resultcode())
                || response.response() == null
                || response.response().id() == null) {
            throw new AuthException(AuthErrorCode.NAVER_AUTH_FAILED);
        }

        // 3. 공통 포맷(SocialProfile)으로 변환
        // 네이버는 ID를 String 타입으로 제공(카카오는 Long 타입)
        NaverUserResponse.Account account = response.response();
        return new SocialProfile(account.id(), account.email(), sanitizeNickname(account.nickname()));
    }

    /**
     * 네이버의 경우 토큰 교환 때 state 파라미터가 필요함 (카카오는 불필요)
     */
    @Override
    public SocialProfile fetchByCode(String code, String state) {

        // 인가 코드로 액세스 토큰 교환
        String accessToken = exchangeCodeForAccessToken(code, state);
        return fetch(accessToken);
    }

    /**
     * 인가 코드 -> 액세스 토큰 교환
     */
    private String exchangeCodeForAccessToken(String code, String state) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        // 1. 인가 코드 제출 로직 (네이버 토큰 발급 API은 JSON이 아닌 폼 형식)
        form.add("grant_type", GRANT_TYPE);
        form.add("client_id", properties.clientId());           // 아필굿 앱 식별자
        form.add("client_secret", properties.clientSecret());   // 네이버는 항상 필수(카카오와 다르게 조건부 아님)
        form.add("code", code);                                 // 인가 코드
        form.add("state", state);                               // login에서 발급한 state와 동일해야 함(네이버 요구사항)

        // 2. 액세스 토큰 발급 로직
        NaverTokenResponse response;
        try {
            response = restClient.post()
                    .uri(properties.tokenUri())                  // 네이버 토큰 발급 URI
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(NaverTokenResponse.class);            // 응답에서 액세스 토큰 필드만 추출
        } catch (RestClientException e) {

            // code가 만료·위조·재사용됐거나 client_secret/state가 안 맞는 경우 네이버가 4xx로 응답
            throw new AuthException(AuthErrorCode.NAVER_AUTH_FAILED);
        }

        if (response == null || response.accessToken() == null) {
            throw new AuthException(AuthErrorCode.NAVER_AUTH_FAILED);
        }

        // 액세스 토큰 반환
        return response.accessToken();
    }

    /**
     * 네이버 사용자 정보 응답 중 우리가 사용하는 필드만 정의
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record NaverUserResponse(
            String resultcode,
            Account response
    ) {

        // 정의 안 한 필드는 무시
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Account(String id, String email, String nickname) {
        }
    }

    /**
     * 네이버 토큰 교환 응답 중 우리가 사용하는 필드만 정의
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record NaverTokenResponse(
            @JsonProperty("access_token")
            String accessToken
    ) {
    }
}
