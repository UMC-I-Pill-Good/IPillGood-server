package com.ipillgood.server.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthRequest {

    // 회원가입 요청
    public record SignUp(
            // 공백 입력
            @NotBlank(message = "1~10자 이내로 입력해주세요.")
            @Pattern(regexp = "^[가-힣a-zA-Z0-9]{1,10}$", message = "1~10자 이내로 입력해주세요.")
            String nickname,

            // '아이디' 필드
            @NotBlank(message = "2~10자 이내로 입력해주세요.")
            @Pattern(regexp = "^[a-zA-Z0-9]{2,10}$", message = "2~10자 이내로 입력해주세요.")
            String username,

            @NotBlank(message = "올바른 이메일 형식이 아닙니다.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            String email,

            @NotBlank(message = "8~16자의 영문, 숫자를 조합해 주세요.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,16}$", message = "8~16자의 영문, 숫자를 조합해 주세요.")
            String password,

            @NotBlank(message = "비밀번호 확인을 입력해주세요.")
            String passwordConfirm
    ) {
    }

    // 로그인 요청
    public record Login(
            @NotBlank(message = "2~10자 이내로 입력해주세요.")
            String username,

            @NotBlank(message = "8~16자의 영문, 숫자를 조합해 주세요.")
            String password
    ) {
    }

    // 액세스 토큰 재발급 요청
    public record Reissue(
            @NotBlank(message = "리프레시 토큰이 필요합니다.")
            String refreshToken
    ) {
    }
}
