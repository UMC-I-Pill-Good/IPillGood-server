package com.ipillgood.server.domain.ingredient.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class IngredientException extends GeneralException {
    public IngredientException(BaseErrorCode code) {
        super(code);
    }
}

