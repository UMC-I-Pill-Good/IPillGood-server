package com.ipillgood.server.domain.ingredient.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum IngredientErrorCode implements BaseErrorCode {

    INVALID_CONTRAINDICATION_TYPE(HttpStatus.BAD_REQUEST, "COMMON400_2", "요청값 검증에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

