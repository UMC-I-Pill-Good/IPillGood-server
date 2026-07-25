package com.ipillgood.server.domain.search.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SearchErrorCode implements BaseErrorCode {

    // 400 BAD_REQUEST
    SEARCH_CURSOR_INVALID(HttpStatus.BAD_REQUEST, "SEARCH400_1", "검색 커서가 올바르지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
