package com.ipillgood.server.domain.notification.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "notification.delivery", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopPushNotificationClient implements PushNotificationClient {

    private static final String FAILURE_REASON = "NOTIFICATION_DELIVERY_DISABLED";

    @Override
    public PushSendResult send(String token, PushNotificationPayload payload) {
        return PushSendResult.retryableFailure(FAILURE_REASON);
    }
}
