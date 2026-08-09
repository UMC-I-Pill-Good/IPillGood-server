package com.ipillgood.server.domain.policy.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PolicySuccessCode implements BaseSuccessCode {

    POLICY_LATEST_FOUND(HttpStatus.OK, "POLICY200_2", "최신 약관/정책 조회에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
