package com.ipillgood.server.domain.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnBean(FirebaseMessaging.class)
public class FirebasePushNotificationClient implements PushNotificationClient {

    private final FirebaseMessaging firebaseMessaging;

    public FirebasePushNotificationClient(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public PushSendResult send(String token, PushNotificationPayload payload) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(toNotification(payload))
                .putData("notificationType", payload.notificationType().name())
                .putData("targetRoute", payload.targetRoute())
                .build();

        try {
            BatchResponse batchResponse = firebaseMessaging.sendEach(List.of(message));
            SendResponse response = batchResponse.getResponses().getFirst();
            if (response.isSuccessful()) {
                return PushSendResult.success();
            }
            return toFailureResult(response.getException());
        } catch (FirebaseMessagingException e) {
            return toFailureResult(e);
        }
    }

    private Notification toNotification(PushNotificationPayload payload) {
        Notification.Builder builder = Notification.builder()
                .setBody(payload.body());
        if (payload.title() != null) {
            builder.setTitle(payload.title());
        }
        return builder.build();
    }

    private PushSendResult toFailureResult(FirebaseMessagingException exception) {
        MessagingErrorCode errorCode = exception.getMessagingErrorCode();
        return toFailureResult(errorCode, exception.getMessage());
    }

    static PushSendResult toFailureResult(MessagingErrorCode errorCode, String message) {
        String failureReason = toFailureReason(errorCode, message);
        if (isInvalidRegistrationTokenError(errorCode)) {
            return PushSendResult.invalidToken(failureReason);
        }
        return PushSendResult.retryableFailure(failureReason);
    }

    private static boolean isInvalidRegistrationTokenError(MessagingErrorCode errorCode) {
        return errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }

    private static String toFailureReason(MessagingErrorCode errorCode, String message) {
        if (errorCode == null) {
            return message;
        }
        if (message == null || message.isBlank()) {
            return errorCode.name();
        }
        return errorCode.name() + ": " + message;
    }
}
