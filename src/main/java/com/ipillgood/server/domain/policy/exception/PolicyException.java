package com.ipillgood.server.domain.policy.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class PolicyException extends GeneralException {
    public PolicyException(BaseErrorCode code) {
        super(code);
    }
}
