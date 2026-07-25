package com.ipillgood.server.domain.search.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class SearchException extends GeneralException {
    public SearchException(BaseErrorCode code) {
        super(code);
    }
}
