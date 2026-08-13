package com.ipillgood.server.domain.review.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class ProductReviewException extends GeneralException {
    public ProductReviewException(BaseErrorCode code) {
        super(code);
    }
}
