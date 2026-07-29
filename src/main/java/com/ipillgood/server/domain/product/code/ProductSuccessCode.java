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
    PRODUCT_INGREDIENTS_VIEW_SUCCESS(HttpStatus.OK, "PRODUCT200_2", "상품의 성분 정보를 성공적으로 조회했습니다."),
    PRODUCT_COMBINATIONS_VIEW_SUCCESS(HttpStatus.OK, "PRODUCT200_3", "상품의 성분 궁합 정보를 성공적으로 조회했습니다."),
    PRODUCT_CAUTION_COMBINATIONS_CHECK_SUCCESS(HttpStatus.OK, "PRODUCT200_4", "섭취 중인 성분들과 함께 복용 시 주의가 필요한 조합을 성공적으로 조회했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
