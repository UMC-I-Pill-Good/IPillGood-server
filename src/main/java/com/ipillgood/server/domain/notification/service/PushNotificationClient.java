package com.ipillgood.server.domain.notification.service;

public interface PushNotificationClient {

    PushSendResult send(String token, PushNotificationPayload payload);
}
