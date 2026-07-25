package com.ipillgood.server.domain.search.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SearchSuccessCode implements BaseSuccessCode {

    // 200 OK
    PRODUCT_SEARCH_SUCCESS(HttpStatus.OK, "SEARCH200_1", "영양제 상품 목록 조회에 성공했습니다."),
    VIEW_RECENT_SEARCH_KEYWORDS_SUCCESS(HttpStatus.OK, "SEARCH200_2", "최근 검색어 조회에 성공했습니다."),
    DELETE_RECENT_SEARCH_KEYWORD_SUCCESS(HttpStatus.OK, "SEARCH200_3", "최근 검색 개별 삭제에 성공했습니다."),
    DELETE_ALL_RECENT_SEARCH_KEYWORDS_SUCCESS(HttpStatus.OK, "SEARCH200_4", "최근 검색어 전체 삭제에 성공했습니다."),

    // 201 CREATED
    STORE_RECENT_SEARCH_KEYWORD_SUCCESS(HttpStatus.CREATED, "SEARCH201_1", "최근 검색어 저장에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
