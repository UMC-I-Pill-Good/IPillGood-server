package com.ipillgood.server.domain.auth.controller;

import com.ipillgood.server.domain.auth.code.AuthSuccessCode;
import com.ipillgood.server.domain.auth.controller.docs.AuthApi;
import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.auth.service.AuthService;
import com.ipillgood.server.domain.auth.service.SocialAuthService;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.security.jwt.CookieUtil;
import com.ipillgood.server.global.security.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final SocialAuthService socialAuthService;
    private final CookieUtil cookieUtil;

    // 로컬 회원가입
    @Override
    @PostMapping("/signup")
    public ApiResponse<AuthResponse.SignUp> signUp(@Valid @RequestBody AuthRequest.SignUp request) {
        AuthResponse.SignUp response = authService.signUp(request);
        return ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_SUCCESS, response);
    }

    // 로컬 로그인
    @Override
    @PostMapping("/login")
    public ApiResponse<AuthResponse.Login> login(@Valid @RequestBody AuthRequest.Login request,
                                                 HttpServletResponse response) {
        AuthResponse.Login result = authService.login(request, response);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, result);
    }

    // 토큰 재발급
    // [액세스 토큰 만료 / 소셜 로그인 콜백 직후 프론트의 accessToken 발급 요청] 둘 다 이 API를 사용
    @Override
    @PostMapping("/reissue")
    public ApiResponse<AuthResponse.Login> reissue(
            @CookieValue(value = CookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        AuthResponse.Login result = authService.reissue(refreshToken, response);
        return ApiResponse.onSuccess(AuthSuccessCode.TOKEN_REISSUE_SUCCESS, result);
    }

    // 로그아웃 (이 기기만 로그아웃, 다른 기기 로그인은 유지)
    @Override
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long memberId,
                                    @RequestAttribute(JwtAuthFilter.SESSION_ID_ATTRIBUTE) String sessionId,
                                    HttpServletResponse response) {
        authService.logout(memberId, sessionId);

        // refreshToken 쿠키 삭제
        cookieUtil.clearRefreshTokenCookie(response);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_SUCCESS, null);
    }

    // 아이디 중복확인
    @Override
    @GetMapping("/check-username")
    public ApiResponse<Void> checkUsername(@RequestParam String username) {
        authService.checkUsernameDuplicate(username);
        return ApiResponse.onSuccess(AuthSuccessCode.USERNAME_AVAILABLE, null);
    }

    // 이메일 중복확인
    @Override
    @GetMapping("/check-email")
    public ApiResponse<Void> checkEmail(@RequestParam String email) {
        authService.checkEmailDuplicate(email);
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_AVAILABLE, null);
    }

    // 카카오 회원가입 (콜백 때 발급한 socialSignupToken + 약관 동의 -> 가입과 동시에 로그인)
    @Override
    @PostMapping("/kakao/signup")
    public ApiResponse<AuthResponse.SocialSignUp> kakaoSignUp(
            @Valid @RequestBody AuthRequest.SocialSignUp request,
            HttpServletResponse response) {

        AuthResponse.SocialSignUp result = socialAuthService.signUp(SocialProvider.KAKAO, request, response);
        return ApiResponse.onSuccess(AuthSuccessCode.SOCIAL_SIGNUP_SUCCESS, result);
    }

    // 네이버 회원가입
    @Override
    @PostMapping("/naver/signup")
    public ApiResponse<AuthResponse.SocialSignUp> naverSignUp(
            @Valid @RequestBody AuthRequest.SocialSignUp request,
            HttpServletResponse response) {

        AuthResponse.SocialSignUp result = socialAuthService.signUp(SocialProvider.NAVER, request, response);
        return ApiResponse.onSuccess(AuthSuccessCode.SOCIAL_SIGNUP_SUCCESS, result);
    }

    // 카카오 계정 연동 (콜백 때 발급한 accountLinkToken -> 연동과 동시에 로그인)
    @Override
    @PostMapping("/kakao/link")
    public ApiResponse<AuthResponse.SocialLink> kakaoLink(
            @Valid @RequestBody AuthRequest.SocialLink request,
            HttpServletResponse response) {

        AuthResponse.SocialLink result = socialAuthService.link(SocialProvider.KAKAO, request, response);
        return ApiResponse.onSuccess(AuthSuccessCode.SOCIAL_LINK_SUCCESS, result);
    }

    // 네이버 계정 연동
    @Override
    @PostMapping("/naver/link")
    public ApiResponse<AuthResponse.SocialLink> naverLink(
            @Valid @RequestBody AuthRequest.SocialLink request,
            HttpServletResponse response) {

        AuthResponse.SocialLink result = socialAuthService.link(SocialProvider.NAVER, request, response);
        return ApiResponse.onSuccess(AuthSuccessCode.SOCIAL_LINK_SUCCESS, result);
    }
}
