package com.ipillgood.server.domain.notification.exception;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

public class NotificationException extends GeneralException {
    public NotificationException(BaseErrorCode code) {
        super(code);
    }
}
