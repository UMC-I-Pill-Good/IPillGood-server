package com.ipillgood.server.domain.auth.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 카카오/네이버 로그인 리다이렉트 API 문서
 * login/callback은 항상 302 응답이라 공통 응답 포맷(ApiResponse)을 쓰지 않음 - 실패도 error 쿼리값으로만 구분
 */
@Tag(name = "Social OAuth Redirect API", description = "카카오/네이버 로그인 리다이렉트(302 전용) 관련 API")
public interface SocialOAuthRedirectApi {

    @Operation(summary = "카카오 로그인",
            description = """
                    카카오 로그인 동의 화면으로 리다이렉트합니다.
                    CSRF 방지용 state를 생성해 oauth_state httpOnly 쿠키(5분)로 심고, 인가 URL에도 동일한 값을 실어 보냅니다.
                    """)
    void kakaoLogin(HttpServletResponse response) throws IOException;

    @Operation(summary = "네이버 로그인", description = "네이버 로그인 동의 화면으로 리다이렉트합니다. 카카오 로그인과 동일한 구조입니다.")
    void naverLogin(HttpServletResponse response) throws IOException;

    @Operation(summary = "카카오 로그인 콜백",
            description = """
                    카카오가 인가 코드와 함께 호출합니다. 프론트가 직접 호출하지 않습니다.
                    항상 302로 응답하며 네 갈래로 나뉩니다:
                    - 로그인 성공(기존 연동 회원): refreshToken을 httpOnly 쿠키로 심고 프론트 콜백 URL로 리다이렉트(쿼리 없음).
                      accessToken은 이 응답에 없습니다 - 프론트가 도착 즉시 POST /auth/reissue를 호출해 받습니다.
                    - 계정 연동 필요(같은 이메일의 기존 회원 존재): 콜백 URL + accountLinkToken + provider 쿼리로 리다이렉트.
                    - 회원가입 필요(완전 신규): 콜백 URL + socialSignupToken + provider 쿼리로 리다이렉트. 이 시점엔 계정을 생성하지 않습니다.
                    - 실패: 콜백 URL + error(에러 코드) 쿼리로 리다이렉트. (AUTH400_13 state 불일치, AUTH400_14 사용자 취소,
                      AUTH400_7 카카오 인증 실패, AUTH400_10 이메일 확인 불가, AUTH400_12 닉네임 확인 불가)
                    """)
    void kakaoCallback(
            @Parameter(description = "카카오 인가 코드 (동의 완료 시)") String code,
            @Parameter(description = "CSRF 검증용 값. oauth_state 쿠키 값과 일치해야 함") String state,
            @Parameter(description = "사용자가 동의 화면에서 취소·거부한 경우 카카오가 내려주는 값") String error,
            @Parameter(description = "httpOnly 쿠키로 전달되는 oauth_state (요청 본문/쿼리 아님)", hidden = true) String oauthStateCookie,
            HttpServletResponse response) throws IOException;

    @Operation(summary = "네이버 로그인 콜백", description = "네이버가 호출한다는 점만 다르고 카카오 로그인 콜백과 동일합니다.")
    void naverCallback(
            @Parameter(description = "네이버 인가 코드 (동의 완료 시)") String code,
            @Parameter(description = "CSRF 검증용 값. oauth_state 쿠키 값과 일치해야 함") String state,
            @Parameter(description = "사용자가 동의 화면에서 취소·거부한 경우 네이버가 내려주는 값") String error,
            @Parameter(description = "httpOnly 쿠키로 전달되는 oauth_state (요청 본문/쿼리 아님)", hidden = true) String oauthStateCookie,
            HttpServletResponse response) throws IOException;
}
