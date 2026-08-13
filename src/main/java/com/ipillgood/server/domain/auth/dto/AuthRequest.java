package com.ipillgood.server.domain.auth.dto;

import com.ipillgood.server.domain.policy.dto.PolicyRequest;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "로컬 회원가입 요청")
    public record SignUp(
            // 공백 입력
            @Schema(description = "닉네임. 공백을 제외한 한글·영문·숫자 1~10자", example = "아필굿")
            @NotBlank(message = "공백을 제외하고 1~10자의 한글, 영문, 숫자를 입력해 주세요.")
            @Pattern(regexp = "^[가-힣a-zA-Z0-9]{1,10}$", message = "공백을 제외하고 1~10자의 한글, 영문, 숫자를 입력해 주세요.")
            String nickname,

            // '아이디' 필드
            @Schema(description = "아이디. 영문·숫자 2~10자", example = "demouser")
            @NotBlank(message = "공백을 제외하고 2~10자의 영문, 숫자를 조합해 주세요.")
            @Pattern(regexp = "^[a-zA-Z0-9]{2,10}$", message = "공백을 제외하고 2~10자의 영문, 숫자를 조합해 주세요.")
            String username,

            @Schema(description = "이메일", example = "demo@ipillgood.com")
            @NotBlank(message = "올바른 이메일 형식이 아닙니다.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            String email,

            @Schema(description = "비밀번호. 영문·숫자·특수문자를 모두 포함한 8~16자", example = "ipillgood1!")
            @NotBlank(message = "8~16자의 영문, 숫자, 특수문자를 조합해 주세요.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+=-])[A-Za-z0-9!@#$%^&*()_+=-]{8,16}$",
                    message = "8~16자의 영문, 숫자, 특수문자를 조합해 주세요.")
            String password,

            @Schema(description = "비밀번호 확인. password와 같은 값", example = "ipillgood1!")
            @NotBlank(message = "비밀번호 확인을 입력해주세요.")
            String passwordConfirm,

            // 약관 동의 목록
            @Schema(description = "약관 동의 목록. 활성 상태인 필수 약관을 모두 agreed=true로 포함")
            @NotNull(message = "약관 동의 정보가 필요합니다.")
            @Valid
            List<PolicyRequest.Agreement> policyAgreements
    ) {
    }

    /**
     * 로컬 로그인 요청
     */
    @Schema(description = "로컬 로그인 요청")
    public record Login(
            @Schema(description = "아이디", example = "demouser")
            @NotBlank(message = "아이디를 입력해주세요.")
            String username,

            @Schema(description = "비밀번호", example = "ipillgood1!")
            @NotBlank(message = "비밀번호를 입력해주세요.")
            String password
    ) {
    }

    /**
     * 소셜 회원가입 요청
     * 이메일/닉네임은 클라이언트에게 받지 않음
     * Redis에 저장한 데이터에 접근할 수 있는 임시 토큰 문자열 키 (socialSignupToken)
     */
    @Schema(description = "소셜 회원가입 요청")
    public record SocialSignUp(
            @Schema(description = "소셜 로그인 콜백이 발급한 회원가입 토큰. 발급 후 5분간 유효한 1회용 값",
                    example = "3f2b1c9a-7d4e-4b58-9c31-8a2f6e0d5b74")
            @NotBlank(message = "소셜 회원가입 토큰이 필요합니다.")
            String socialSignupToken,

            // 약관 동의 목록
            @Schema(description = "약관 동의 목록. 활성 상태인 필수 약관을 모두 agreed=true로 포함")
            @NotNull(message = "약관 동의 정보가 필요합니다.")
            @Valid
            List<PolicyRequest.Agreement> policyAgreements
    ) {
    }

    /**
     * 소셜 계정 연동 요청
     * 연동 대상 회원·소셜 정보는 임시 토큰이 들고 있으므로 토큰만 받음
     */
    @Schema(description = "소셜 계정 연동 요청")
    public record SocialLink(
            @Schema(description = "소셜 로그인 콜백이 발급한 계정 연동 토큰. 발급 후 5분간 유효한 1회용 값",
                    example = "8c5d0e21-4a97-4f36-b8d2-1e7c93a5f024")
            @NotBlank(message = "계정 연동 토큰이 필요합니다.")
            String accountLinkToken
    ) {
    }
}
