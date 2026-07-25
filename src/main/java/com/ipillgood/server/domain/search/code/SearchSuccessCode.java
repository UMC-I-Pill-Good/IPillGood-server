package com.ipillgood.server.domain.search.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SearchSuccessCode implements BaseSuccessCode {

    // 200 OK
    PRODUCT_SEARCH_SUCCESS(HttpStatus.OK, "SUCCESS200_1", "영양제 상품 목록 조회에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
