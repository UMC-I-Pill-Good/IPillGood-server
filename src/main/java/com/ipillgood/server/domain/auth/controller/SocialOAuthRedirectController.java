package com.ipillgood.server.domain.auth.controller;

import com.ipillgood.server.domain.auth.client.SocialProperties;
import com.ipillgood.server.domain.auth.code.AuthErrorCode;
import com.ipillgood.server.domain.auth.controller.docs.SocialOAuthRedirectApi;
import com.ipillgood.server.domain.auth.exception.AuthException;
import com.ipillgood.server.domain.auth.service.SocialAuthService;
import com.ipillgood.server.domain.auth.service.SocialCallbackResult;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import com.ipillgood.server.global.security.jwt.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

/**
 * 카카오/네이버 로그인, 콜백 [리다이렉트 전용 컨트롤러] - 지역 핸들러 사용 (글로벌 핸들러 X)
 * 응답이 항상 302 리다이렉트라 공통 응답 형식(JSON)을 쓰지 않고, 예외도 JSON이 아닌 리다이렉트로 반환
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class SocialOAuthRedirectController implements SocialOAuthRedirectApi {

    private final SocialAuthService socialAuthService;
    private final CookieUtil cookieUtil;
    private final SocialProperties socialProperties;

    @Value("${app.frontend.callback-url}")
    private String frontendCallbackUrl;

    @Override
    @GetMapping("/kakao/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        redirectToProvider(SocialProvider.KAKAO, response);
    }

    @Override
    @GetMapping("/naver/login")
    public void naverLogin(HttpServletResponse response) throws IOException {
        redirectToProvider(SocialProvider.NAVER, response);
    }

    @Override
    @GetMapping("/kakao/callback")
    public void kakaoCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @CookieValue(value = CookieUtil.OAUTH_STATE_COOKIE_NAME, required = false) String oauthStateCookie,
            HttpServletResponse response) throws IOException {

        processCallback(SocialProvider.KAKAO, code, state, error, oauthStateCookie, response);
    }

    @Override
    @GetMapping("/naver/callback")
    public void naverCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @CookieValue(value = CookieUtil.OAUTH_STATE_COOKIE_NAME, required = false) String oauthStateCookie,
            HttpServletResponse response) throws IOException {

        processCallback(SocialProvider.NAVER, code, state, error, oauthStateCookie, response);
    }

    /**
     * 카카오/네이버 동의 화면으로 리다이렉트
     */
    private void redirectToProvider(SocialProvider provider, HttpServletResponse response) throws IOException {

        // CSRF 방지용 state 생성 후 쿠키에 포함
        String state = UUID.randomUUID().toString();
        cookieUtil.setOauthStateCookie(response, state);

        ProviderOAuthConfig config = configFor(provider);
        String location = UriComponentsBuilder.fromUriString(config.authorizeUri())
                .queryParam("client_id", config.clientId())
                .queryParam("redirect_uri", config.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("state", state)     // 브라우저 URL 쿼리에도 state를 넣어 추후 쿠키 속 state와 비교 검증
                .build()
                .toUriString();

        response.sendRedirect(location);
    }

    /**
     * 콜백 처리 - state 검증 -> 사용자 취소 확인 -> 서비스에 4분기 판정 위임 -> 결과에 따라 리다이렉트
     * 실패는 여기서 잡지 않고 그냥 던짐 (지역 @ExceptionHandler가 리다이렉트로 변환)
     */
    private void processCallback(SocialProvider provider, String code, String state, String error,
                                 String oauthStateCookie, HttpServletResponse response) throws IOException {

        // 1. oauth_state 쿠키는 state 검증용 일회용 쿠키이므로 성공/실패 여부와 무관하게 우선 제거
        cookieUtil.clearOauthStateCookie(response);

        // 2. 쿼리 속 state와 쿠키 속 state 비교를 통해 CSRF 방지(state가 서로 다르면 CSRF 의심 -> 예외 처리)
        if (!StringUtils.hasText(state) || !state.equals(oauthStateCookie)) {
            throw new AuthException(AuthErrorCode.STATE_MISMATCH);
        }

        // 3. error 쿼리에 값이 있는 경우 - 카카오/네이버 동의 화면(닉네임/이메일 필수 동의)에서 사용자가 취소한 경우
        if (StringUtils.hasText(error)) {
            throw new AuthException(AuthErrorCode.SOCIAL_LOGIN_CANCELLED);
        }

        // 4. 2·3번 조건을 통과한 경우 4갈래 판정(로그인 성공/연동/가입 필요/실패) 진행
        SocialCallbackResult result = socialAuthService.handleCallback(provider, code, state, response);

        // 5. 전부 통과한 단계. result(로그인 성공/연동/가입 필요)를 실제 리다이렉트 URL로 바꿔 응답 보냄
        response.sendRedirect(buildResultRedirect(provider, result));
    }

    private String buildResultRedirect(SocialProvider provider, SocialCallbackResult result) {
        return switch (result.status()) {

            // 1. 로그인 성공 -> 즉시 로그인
            case LOGIN_SUCCESS -> frontendCallbackUrl;

            // 2. 계정 연동 필요 (동일한 이메일로 다른 가입 이력 존재)
            case LINK_REQUIRED -> UriComponentsBuilder.fromUriString(frontendCallbackUrl)
                    .queryParam("accountLinkToken", result.accountLinkToken())
                    .queryParam("provider", provider.name().toLowerCase())
                    .build()
                    .toUriString();

            // 3. 신규 가입자 -> 회원가입 필요
            case SIGNUP_REQUIRED -> UriComponentsBuilder.fromUriString(frontendCallbackUrl)
                    .queryParam("socialSignupToken", result.socialSignupToken())
                    .build()
                    .toUriString();
        };
    }

    // 이 컨트롤러 클래스의 예외를 전부 리다이렉트로 변환
    @ExceptionHandler(GeneralException.class)
    public void handleGeneralException(GeneralException e, HttpServletResponse response) throws IOException {
        redirectWithError(e.getCode().getCode(), response);
    }

    // 정의되지 않은 예외는 COMMON500_1로 리다이렉트
    @ExceptionHandler(Exception.class)
    public void handleUnexpectedException(Exception e, HttpServletResponse response) throws IOException {

        // 로그 남기기
        log.error("소셜 로그인 리다이렉트 처리 중 예기치 못한 오류", e);

        // 사용자한테는 그냥 COMMON500_1 응답 처리
        redirectWithError(GeneralErrorCode.INTERNAL_SERVER_ERROR.getCode(), response);
    }

    // 두 지역 핸들러의 헬퍼 메서드. 프론트 콜백 URL에 `?error=xxx`을 붙여서 302 리다이렉트
    private void redirectWithError(String errorCode, HttpServletResponse response) throws IOException {
        String location = UriComponentsBuilder.fromUriString(frontendCallbackUrl)
                .queryParam("error", errorCode)
                .build()
                .toUriString();
        response.sendRedirect(location);
    }

    private ProviderOAuthConfig configFor(SocialProvider provider) {
        return switch (provider) {
            case KAKAO -> new ProviderOAuthConfig(
                    socialProperties.kakao().authorizeUri(),
                    socialProperties.kakao().clientId(),
                    socialProperties.kakao().redirectUri());
            case NAVER -> new ProviderOAuthConfig(
                    socialProperties.naver().authorizeUri(),
                    socialProperties.naver().clientId(),
                    socialProperties.naver().redirectUri());
        };
    }

    private record ProviderOAuthConfig(String authorizeUri, String clientId, String redirectUri) {
    }
}
