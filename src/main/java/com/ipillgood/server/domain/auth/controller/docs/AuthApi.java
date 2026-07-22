package com.ipillgood.server.domain.auth.controller.docs;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            description = "아이디와 비밀번호를 입력받아 로그인을 진행합니다.")
    ApiResponse<AuthResponse.Login> login(@Valid AuthRequest.Login request);

    @Operation(summary = "토큰 재발급",
            description = "리프레시 토큰으로 액세스/리프레시 토큰을 재발급합니다. 재발급 시 리프레시 토큰은 회전(RTR)됩니다.")
    ApiResponse<AuthResponse.Login> reissue(@Valid AuthRequest.Reissue request);

    @Operation(summary = "로그아웃",
            description = "저장된 리프레시 토큰을 폐기합니다. (액세스 토큰은 만료 시점까지 유효)")
    ApiResponse<Void> logout(Long memberId);

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
                    - 이미 연동된 소셜 계정이면 바로 로그인합니다. (accessToken, refreshToken 발급)
                    - 같은 이메일의 기존 회원이 존재하면 연동 동의 필요합니다. (accountLinkRequired=true + accountLinkToken 발급)
                    - 신규 사용자라면 회원가입 필요합니다. (signupRequired=true)
                    이메일 제공에 동의하지 않은 경우 로그인에 실패합니다. (AUTH400_10)
                    """)
    ApiResponse<AuthResponse.SocialLogin> socialLogin(String provider, @Valid AuthRequest.SocialLogin request);
}
