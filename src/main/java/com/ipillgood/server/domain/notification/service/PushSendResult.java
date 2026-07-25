package com.ipillgood.server.domain.notification.service;

public record PushSendResult(
        boolean successful,
        boolean invalidToken,
        String failureReason
) {

    public static PushSendResult success() {
        return new PushSendResult(true, false, null);
    }

    public static PushSendResult invalidToken(String failureReason) {
        return new PushSendResult(false, true, failureReason);
    }

    public static PushSendResult retryableFailure(String failureReason) {
        return new PushSendResult(false, false, failureReason);
    }
}
