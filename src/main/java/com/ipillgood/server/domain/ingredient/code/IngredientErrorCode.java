package com.ipillgood.server.domain.ingredient.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum IngredientErrorCode implements BaseErrorCode {

    // 400 BAD_REQUEST - 영양성분 요청 값 검증
    INVALID_INGREDIENT_ID(HttpStatus.BAD_REQUEST, "INGREDIENT400_1", "영양성분 ID가 올바르지 않습니다."),

    // 404 NOT_FOUND - 영양성분 리소스 조회 실패
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "INGREDIENT404_1", "영양성분을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
