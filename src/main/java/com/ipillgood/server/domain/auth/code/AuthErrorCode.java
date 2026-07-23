package com.ipillgood.server.domain.auth.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // 400 BAD_REQUEST - 유효성 검사
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "AUTH400_1", "1~10자 이내로 입력해주세요."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "AUTH400_2", "올바른 이메일 형식이 아닙니다."),
    USERNAME_CHECK_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH400_3", "아이디 중복 확인을 해주세요."),
    INVALID_USERNAME_FORMAT(HttpStatus.BAD_REQUEST, "AUTH400_4", "2~10자 이내로 입력해주세요."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "AUTH400_5", "8~16자의 영문, 숫자를 조합해 주세요."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH400_6", "비밀번호가 일치하지 않습니다."),
    KAKAO_AUTH_FAILED(HttpStatus.BAD_REQUEST, "AUTH400_7", "카카오 로그인에 실패했습니다. 다시 시도해주세요."),
    NAVER_AUTH_FAILED(HttpStatus.BAD_REQUEST, "AUTH400_8", "네이버 로그인에 실패했습니다. 다시 시도해주세요."),
    // AUTH400_9(필수 약관 미동의)는 PolicyErrorCode에 위치
    SOCIAL_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH400_10", "소셜 계정 이메일을 확인할 수 없습니다. 이메일 제공에 동의해주세요."),
    UNSUPPORTED_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH400_11", "지원하지 않는 소셜 로그인입니다."),
    SOCIAL_NICKNAME_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH400_12", "소셜 계정 닉네임을 확인할 수 없습니다. 프로필 제공에 동의해주세요."),

    // 401 UNAUTHORIZED - 인증 실패
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH401_1", "아이디 또는 비밀번호를 확인해주세요."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH401_2", "다시 로그인해 주세요."),

    // 409 CONFLICT - 중복/계정 충돌
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH409_1", "이미 사용 중인 이메일입니다. 해당 이메일로 로그인해 주세요."),
    SOCIAL_ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH409_2",
            "이미 해당 이메일로 [카카오/네이버] 계정이 존재해요. 해당 소셜 계정으로 로그인해 주세요."),
    ACCOUNT_LINK_REQUIRED(HttpStatus.CONFLICT, "AUTH409_3", "이미 해당 이메일로 가입된 계정이 있어요. 기존 계정에 [카카오/네이버] 로그인을 연동하시겠어요?"),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "AUTH409_4", "이미 사용 중인 아이디입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
