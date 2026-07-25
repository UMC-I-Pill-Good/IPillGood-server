package com.ipillgood.server.domain.support.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SupportSuccessCode implements BaseSuccessCode {

    FAQ_LIST_FOUND(HttpStatus.OK, "SUPPORT200_1", "FAQ 목록 조회에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
