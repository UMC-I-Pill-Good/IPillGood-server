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

    // 403 FORBIDDEN
    RECENT_KEYWORD_FORBIDDEN(HttpStatus.FORBIDDEN, "SEARCH403_1", "해당 키워드 삭제 권한이 없습니다."),

    // 404 NOT_FOUND
    SEARCH_KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "SEARCH404_1", "검색 키워드가 존재하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
