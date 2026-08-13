package com.ipillgood.server.domain.recommendation.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class RecommendationException extends GeneralException {
    public RecommendationException(BaseErrorCode code) {
        super(code);
    }
}
