package com.ipillgood.server.domain.product.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductSuccessCode implements BaseSuccessCode {

    // 200 OK
    PRODUCT_VIEW_SUCCESS(HttpStatus.OK, "PRODUCT200_1", "상품 상세 정보를 성공적으로 조회했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
