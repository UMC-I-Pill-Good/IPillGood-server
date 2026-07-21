package com.ipillgood.server.domain.survey.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class SurveyException extends GeneralException {
    public SurveyException(BaseErrorCode code) {
        super(code);
    }
}
