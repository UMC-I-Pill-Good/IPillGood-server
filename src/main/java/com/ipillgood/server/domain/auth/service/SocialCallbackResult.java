package com.ipillgood.server.domain.auth.service;

/**
 * SocialAuthService.handleCallback()의 반환 타입 - AuthConverter(JSON 응답 전용)로 return 불가능
 * 따라서, 컨트롤러 <-> 서비스 계층이 공유하는 record 클래스로 분리
 */
public record SocialCallbackResult(

        // 소셜 로그인 콜백 판정 결과 status - [연동 필요/즉시 로그인/가입 필요]
        Status status,

        String accountLinkToken,
        String socialSignupToken
) {

    public enum Status {
        LOGIN_SUCCESS,
        LINK_REQUIRED,
        SIGNUP_REQUIRED
    }

    public static SocialCallbackResult loginSuccess() {
        return new SocialCallbackResult(Status.LOGIN_SUCCESS, null, null);
    }

    public static SocialCallbackResult linkRequired(String accountLinkToken) {
        return new SocialCallbackResult(Status.LINK_REQUIRED, accountLinkToken, null);
    }

    public static SocialCallbackResult signupRequired(String socialSignupToken) {
        return new SocialCallbackResult(Status.SIGNUP_REQUIRED, null, socialSignupToken);
    }
}
