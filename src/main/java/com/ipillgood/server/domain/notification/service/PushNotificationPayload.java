package com.ipillgood.server.domain.notification.service;

import com.ipillgood.server.domain.notification.entity.enums.NotificationType;

public record PushNotificationPayload(
        NotificationType notificationType,
        String title,
        String body,
        String targetRoute
) {
}
