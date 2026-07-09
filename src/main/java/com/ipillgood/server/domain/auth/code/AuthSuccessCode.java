package com.ipillgood.server.domain.auth.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    SIGNUP_SUCCESS(HttpStatus.CREATED, "AUTH201_1", "회원가입이 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "AUTH200_1", "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH200_2", "로그아웃에 성공했습니다."),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "AUTH200_3", "토큰 재발급에 성공했습니다."),
    USERNAME_AVAILABLE(HttpStatus.OK, "AUTH200_4", "사용 가능한 아이디입니다."),
    EMAIL_AVAILABLE(HttpStatus.OK, "AUTH200_5", "사용 가능한 이메일입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
