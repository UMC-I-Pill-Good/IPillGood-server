package com.ipillgood.server.domain.intake.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class IntakeException extends GeneralException {
    public IntakeException(BaseErrorCode code) {
        super(code);
    }
}

