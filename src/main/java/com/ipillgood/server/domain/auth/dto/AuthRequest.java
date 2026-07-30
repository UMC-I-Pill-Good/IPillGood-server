package com.ipillgood.server.domain.auth.dto;

import com.ipillgood.server.domain.policy.dto.PolicyRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public class AuthRequest {

    /**
     * 로컬 회원가입 요청
     */
    public record SignUp(
            // 공백 입력
            @NotBlank(message = "공백을 제외하고 한글/영문/숫자만 1~10자 이내로 입력해주세요.")
            @Pattern(regexp = "^[가-힣a-zA-Z0-9]{1,10}$", message = "공백을 제외하고 한글/영문/숫자만 1~10자 이내로 입력해주세요.")
            String nickname,

            // '아이디' 필드
            @NotBlank(message = "2~10자 이내로 입력해주세요.")
            @Pattern(regexp = "^[a-zA-Z0-9]{2,10}$", message = "2~10자 이내로 입력해주세요.")
            String username,

            @NotBlank(message = "올바른 이메일 형식이 아닙니다.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            String email,

            @NotBlank(message = "8~16자의 영문, 숫자, 특수문자를 조합해 주세요.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+=-])[A-Za-z0-9!@#$%^&*()_+=-]{8,16}$",
                    message = "8~16자의 영문, 숫자, 특수문자를 조합해 주세요.")
            String password,

            @NotBlank(message = "비밀번호 확인을 입력해주세요.")
            String passwordConfirm,

            // 약관 동의 목록
            @NotNull(message = "약관 동의 정보가 필요합니다.")
            @Valid
            List<PolicyRequest.Agreement> policyAgreements
    ) {
    }

    /**
     * 로컬 로그인 요청
     */
    public record Login(
            @NotBlank(message = "2~10자 이내로 입력해주세요.")
            String username,

            @NotBlank(message = "8~16자의 영문, 숫자를 조합해 주세요.")
            String password
    ) {
    }

    /**
     * 소셜 로그인 요청
     * 이메일은 클라이언트에게 받지 않고 서버가 소셜 제공자에게 직접 조회함
     */
    public record SocialLogin(
            @NotBlank(message = "소셜 액세스 토큰이 필요합니다.")
            String providerAccessToken
    ) {
    }

    /**
     * 소셜 회원가입 요청
     * 닉네임은 클라이언트에게 받지 않고 서버가 소셜 제공자에게 직접 조회
     */
    public record SocialSignUp(
            @NotBlank(message = "소셜 액세스 토큰이 필요합니다.")
            String providerAccessToken,

            // 약관 동의 목록
            @NotNull(message = "약관 동의 정보가 필요합니다.")
            @Valid
            List<PolicyRequest.Agreement> policyAgreements
    ) {
    }

    /**
     * 소셜 계정 연동 요청
     * 연동 대상 회원·소셜 정보는 임시 토큰이 들고 있으므로 토큰만 받음
     */
    public record SocialLink(
            @NotBlank(message = "계정 연동 토큰이 필요합니다.")
            String accountLinkToken
    ) {
    }
}
