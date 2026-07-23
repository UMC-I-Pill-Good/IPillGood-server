package com.ipillgood.server.domain.condition.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class ConditionException extends GeneralException {
    public ConditionException(BaseErrorCode code) {
        super(code);
    }
}
