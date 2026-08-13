package com.ipillgood.server.domain.product.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class ProductException extends GeneralException {
    public ProductException(BaseErrorCode code) {
        super(code);
    }
}
