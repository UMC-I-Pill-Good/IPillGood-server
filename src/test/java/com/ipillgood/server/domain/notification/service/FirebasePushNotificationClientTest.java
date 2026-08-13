package com.ipillgood.server.domain.notification.service;

import com.google.firebase.messaging.MessagingErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebasePushNotificationClientTest {

    @Test
    @DisplayName("UNREGISTERED는 토큰 비활성화 대상 실패로 분류한다")
    void toFailureResult_withUnregistered_returnsInvalidToken() {
        PushSendResult result = FirebasePushNotificationClient.toFailureResult(
                MessagingErrorCode.UNREGISTERED,
                "token is unregistered"
        );

        assertFalse(result.successful());
        assertTrue(result.invalidToken());
    }

    @Test
    @DisplayName("INVALID_ARGUMENT는 토큰 비활성화 대상에서 제외한다")
    void toFailureResult_withInvalidArgument_returnsRetryableFailure() {
        PushSendResult result = FirebasePushNotificationClient.toFailureResult(
                MessagingErrorCode.INVALID_ARGUMENT,
                "registration token is invalid"
        );

        assertFalse(result.successful());
        assertFalse(result.invalidToken());
    }

    @Test
    @DisplayName("SENDER_ID_MISMATCH는 토큰 비활성화 대상에서 제외한다")
    void toFailureResult_withSenderIdMismatch_returnsRetryableFailure() {
        PushSendResult result = FirebasePushNotificationClient.toFailureResult(
                MessagingErrorCode.SENDER_ID_MISMATCH,
                "sender id mismatch"
        );

        assertFalse(result.successful());
        assertFalse(result.invalidToken());
    }
}
