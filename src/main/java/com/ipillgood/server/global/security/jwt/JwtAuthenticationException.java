package com.ipillgood.server.global.security.jwt;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class JwtAuthenticationException extends GeneralException {
    public JwtAuthenticationException(BaseErrorCode code) {
        super(code);
    }
}
