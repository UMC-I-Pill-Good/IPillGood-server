package com.ipillgood.server.domain.ingredient.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum IngredientSuccessCode implements BaseSuccessCode {

    CONTRAINDICATION_LIST_SUCCESS(HttpStatus.OK, "SUCCESS200_1", "금기 조건 목록 조회에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

