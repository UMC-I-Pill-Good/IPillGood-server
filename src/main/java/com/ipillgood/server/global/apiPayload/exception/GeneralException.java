package com.ipillgood.server.global.apiPayload.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private final BaseErrorCode code;
}
