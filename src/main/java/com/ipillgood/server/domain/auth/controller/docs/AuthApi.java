package com.ipillgood.server.domain.auth.controller.docs;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 인증 관련 API 문서
 * 회원가입, 로그인, 아이디/이메일 중복검사
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

    @Operation(summary = "소셜 로그인",
            description = """
                    소셜 액세스 토큰을 검증하고 사용자 상태에 따라 세 경우로 응답합니다.
                    - 이미 연동된 소셜 계정이면 바로 로그인합니다. (accessToken은 Body로, refreshToken은 httpOnly 쿠키로 발급)
                    - 같은 이메일의 기존 회원이 존재하면 연동 동의 필요합니다. (accountLinkRequired=true + accountLinkToken 발급)
                    - 신규 사용자라면 회원가입 필요합니다. (signupRequired=true)
                    이메일 제공에 동의하지 않은 경우 로그인에 실패합니다. (AUTH400_10)
                    """)
    ApiResponse<AuthResponse.SocialLogin> socialLogin(
            @Parameter(
                    description = "소셜 제공자입니다.",
                    schema = @Schema(
                            allowableValues = {"KAKAO", "NAVER"},
                            defaultValue = "KAKAO"
                    )
            )
            String provider,
            @Valid AuthRequest.SocialLogin request,
            HttpServletResponse response);

    @Operation(summary = "소셜 회원가입",
            description = """
                    소셜 신규 사용자의 약관 동의를 받아 회원가입을 완료합니다.
                    닉네임은 요청으로 받지 않고 서버가 소셜 제공자에게 직접 조회합니다.
                    자동 로그인하지 않으므로 토큰을 발급하지 않습니다. (가입 완료 후 로그인 화면으로 이동)
                    이메일/닉네임 제공에 동의하지 않은 경우 실패합니다. (AUTH400_10, AUTH400_12)
                    """)
    ApiResponse<AuthResponse.SocialSignUp> socialSignUp(
            @Parameter(
                    description = "소셜 제공자입니다.",
                    schema = @Schema(
                            allowableValues = {"KAKAO", "NAVER"},
                            defaultValue = "KAKAO"
                    )
            )
            String provider,
            @Valid AuthRequest.SocialSignUp request);

    @Operation(summary = "소셜 계정 연동",
            description = """
                    소셜 로그인 중 발급받은 임시 토큰으로 기존 회원에 소셜 계정을 연동합니다.
                    연동 즉시 로그인 처리되어 토큰을 함께 발급합니다. (accessToken은 Body로, refreshToken은 httpOnly 쿠키로 발급)
                    임시 토큰이 만료·위조되었거나 URL의 제공자와 다르면 실패합니다. (AUTH401_3)
                    """)
    ApiResponse<AuthResponse.SocialLink> socialLink(
            @Parameter(
                    description = "소셜 제공자입니다.",
                    schema = @Schema(
                            allowableValues = {"KAKAO", "NAVER"},
                            defaultValue = "KAKAO"
                    )
            )
            String provider,
            @Valid AuthRequest.SocialLink request,
            HttpServletResponse response);
}
