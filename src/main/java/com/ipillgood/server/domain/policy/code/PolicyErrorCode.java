package com.ipillgood.server.domain.policy.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PolicyErrorCode implements BaseErrorCode {

    // 400 BAD_REQUEST - 약관 동의 검증
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "AUTH400_9", "필수 항목에 동의해주세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
