package com.ipillgood.server.domain.auth.controller.docs;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 인증 관련 API 문서
 * 회원가입, 로그인, 아이디/이메일 중복검사, 소셜 회원가입/계정연동(JSON 응답)
 * 소셜 로그인/콜백(302 리다이렉트 전용)은 SocialOAuthRedirectApi 참고
 */
@Tag(name = "Auth API", description = "인증(회원가입/로그인) 관련 API")
public interface AuthApi {

    @Operation(summary = "로컬 회원가입",
            description = "닉네임/아이디/이메일/비밀번호를 입력받아 회원가입을 진행합니다.")
    ApiResponse<AuthResponse.SignUp> signUp(@Valid AuthRequest.SignUp request);

    @Operation(summary = "로컬 로그인",
            description = "아이디와 비밀번호를 입력받아 로그인을 진행합니다. refreshToken은 httpOnly 쿠키로 발급됩니다.")
    ApiResponse<AuthResponse.Login> login(@Valid AuthRequest.Login request, HttpServletResponse response);

    @Operation(summary = "토큰 재발급",
            description = """
                    httpOnly 쿠키의 refreshToken을 검증해 액세스 토큰을 재발급합니다. 재발급 시 refreshToken도 새 토큰으로 재발급됩니다.
                    액세스 토큰 만료 시 재발급 요청, 소셜 로그인 콜백 직후 프론트가 accessToken을 받을 때 모두 이 API를 사용합니다.
                    """)
    ApiResponse<AuthResponse.Login> reissue(
            @Parameter(description = "httpOnly 쿠키로 전달되는 refreshToken (요청 본문 아님)", hidden = true)
            String refreshToken,
            HttpServletResponse response);

    @Operation(summary = "로그아웃",
            description = "이 기기(세션)의 리프레시 토큰만 폐기합니다. 다른 기기에서의 로그인은 유지됩니다. refreshToken 쿠키도 함께 삭제됩니다. (액세스 토큰은 만료 시점까지 유효)")
    ApiResponse<Void> logout(Long memberId, String sessionId, HttpServletResponse response);

    @Operation(summary = "아이디 중복확인", description = "입력한 아이디가 이미 사용 중인지 확인합니다.")
    ApiResponse<Void> checkUsername(
            @Pattern(regexp = "^[a-zA-Z0-9]{2,10}$", message = "2~10자 이내로 입력해주세요.")
            String username);

    @Operation(summary = "이메일 중복확인", description = "입력한 이메일이 이미 사용 중인지 확인합니다.")
    ApiResponse<Void> checkEmail(
            @NotBlank(message = "올바른 이메일 형식이 아닙니다.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            String email);

    @Operation(summary = "카카오 회원가입",
            description = """
                    카카오 로그인 콜백이 신규 유저로 판정하며 발급한 socialSignupToken과 약관 동의를 받아 회원가입을 완료합니다.
                    이메일/닉네임은 요청으로 받지 않습니다 - 콜백이 조회해 socialSignupToken에 연결해둔 값을 그대로 씁니다.
                    회원가입과 동시에 로그인 처리됩니다. (accessToken은 Body로, refreshToken은 httpOnly 쿠키로 발급)
                    socialSignupToken이 만료·위조·이미 사용됐거나 provider가 KAKAO가 아니면 실패합니다. (AUTH401_3)
                    콜백 후 약관 동의 화면에 있는 동안 같은 이메일로 가입이 된 경우 가입에 실패합니다.
                    """)
    ApiResponse<AuthResponse.SocialSignUp> kakaoSignUp(@Valid AuthRequest.SocialSignUp request,
                                                       HttpServletResponse response);

    @Operation(summary = "네이버 회원가입",
            description = "네이버 로그인 콜백 기준이라는 점만 다르고 카카오 회원가입과 동일합니다.")
    ApiResponse<AuthResponse.SocialSignUp> naverSignUp(@Valid AuthRequest.SocialSignUp request,
                                                       HttpServletResponse response);

    @Operation(summary = "카카오 계정 연동",
            description = """
                    카카오 로그인 콜백이 계정 연동 필요로 판정하며 발급한 accountLinkToken으로 기존 회원에 카카오 계정을 연동합니다.
                    연동 즉시 로그인 처리되어 토큰을 함께 발급합니다. (accessToken은 Body로, refreshToken은 httpOnly 쿠키로 발급)
                    이 엔드포인트는 카카오 전용입니다 - 토큰 속 provider가 KAKAO가 아니면 조작으로 보고 차단합니다. (AUTH401_3)
                    """)
    ApiResponse<AuthResponse.SocialLink> kakaoLink(@Valid AuthRequest.SocialLink request,
                                                   HttpServletResponse response);

    @Operation(summary = "네이버 계정 연동",
            description = "네이버 전용이라는 점만 다르고 카카오 계정 연동과 동일합니다.")
    ApiResponse<AuthResponse.SocialLink> naverLink(@Valid AuthRequest.SocialLink request,
                                                   HttpServletResponse response);
}
