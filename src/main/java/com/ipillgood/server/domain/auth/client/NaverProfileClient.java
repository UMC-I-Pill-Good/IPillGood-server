package com.ipillgood.server.domain.auth.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ipillgood.server.domain.auth.client.dto.SocialProfile;
import com.ipillgood.server.domain.auth.code.AuthErrorCode;
import com.ipillgood.server.domain.auth.exception.AuthException;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 네이버 사용자 정보 조회 클라이언트 (우리 서버 -> 네이버 API)
 * - 앱이 네이버 SDK로 받아 전달한 액세스 토큰을 네이버에 되물어 진위를 확인함
 * - 응답에서 고유 ID와 이메일만 뽑아 공통 포맷(SocialProfile)으로 변환함
 */
@Component
public class NaverProfileClient implements SocialProfileClient {

    private static final String BEARER_PREFIX = "Bearer ";

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
}
