package com.ipillgood.server.domain.auth.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ipillgood.server.domain.auth.client.dto.SocialProfile;
import com.ipillgood.server.domain.auth.code.AuthErrorCode;
import com.ipillgood.server.domain.auth.exception.AuthException;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 카카오 사용자 정보 조회 클라이언트 (우리 서버 -> 카카오 API)
 * 앱이 카카오 SDK로 받아 전달한 액세스 토큰으로 사용자 확인
 * 응답에서 회원번호, 이메일 값을 공통 포맷(SocialProfile)으로 변환
 */
@Component
public class KakaoProfileClient implements SocialProfileClient {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SocialProperties.Kakao properties;
    private final RestClient restClient;

    public KakaoProfileClient(SocialProperties socialProperties, RestClient socialRestClient) {
        this.properties = socialProperties.kakao();
        this.restClient = socialRestClient;
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public SocialProfile fetch(String providerAccessToken) {

        // 1. 우리 앱에서 발급된 토큰인지 확인
        verifyIssuedForThisApp(providerAccessToken);

        // 2. 액세스 토큰으로 카카오에 사용자 정보 요청
        KakaoUserResponse response = get(properties.userInfoUri(), providerAccessToken, KakaoUserResponse.class);

        // 3. 회원번호로 사용자 식별, 없으면 실패 처리
        if (response == null || response.id() == null) {
            throw new AuthException(AuthErrorCode.KAKAO_AUTH_FAILED);
        }

        // 4. 이메일 추출
        // 사용자가 이메일 제공에 동의하지 않았을 경우 null로 전달
        String email = response.kakaoAccount() == null ? null : response.kakaoAccount().email();

        // 5. 공통 포맷(SocialProfile)으로 변환
        // 카카오는 회원번호를 Long 타입으로 주므로 String 타입으로 통일
        return new SocialProfile(String.valueOf(response.id()), email);
    }

    /**
     * 토큰 치환 방어: 다른 앱에서 발급된 액세스 토큰이 우리 서버로 들어오는 것을 차단
     */
    private void verifyIssuedForThisApp(String providerAccessToken) {
        if (!StringUtils.hasText(properties.appId())) {
            return;
        }

        KakaoTokenInfoResponse tokenInfo =
                get(properties.tokenInfoUri(), providerAccessToken, KakaoTokenInfoResponse.class);

        if (tokenInfo == null || !properties.appId().equals(String.valueOf(tokenInfo.appId()))) {
            throw new AuthException(AuthErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    /**
     * 카카오 API 공통 GET 호출 (사용자 정보, 토큰 정보) - 응답 Type만 다름
     */
    private <T> T get(String uri, String providerAccessToken, Class<T> responseType) {
        try {
            return restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + providerAccessToken)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientException e) {

            // 토큰이 만료·위조된 경우 카카오가 4xx로 응답 + RestClient가 예외 처리
            throw new AuthException(AuthErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    /**
     * 카카오 사용자 정보 응답 중 우리가 사용하는 필드만 정의
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record KakaoUserResponse(
            Long id,

            @JsonProperty("kakao_account")
            KakaoAccount kakaoAccount
    ) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        record KakaoAccount(String email) {
        }
    }

    /**
     * 카카오 토큰 정보 응답 중 우리가 사용하는 필드만 정의
     * appId: 해당 토큰을 발급한 앱의 ID
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record KakaoTokenInfoResponse(

            @JsonProperty("app_id")
            Long appId
    ) {
    }
}
