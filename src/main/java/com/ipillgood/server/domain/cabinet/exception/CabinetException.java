package com.ipillgood.server.domain.cabinet.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class CabinetException extends GeneralException {
    public CabinetException(BaseErrorCode code) {
        super(code);
    }
}
