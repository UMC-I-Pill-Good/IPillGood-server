package com.ipillgood.server.domain.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(
        Delivery delivery,
        Firebase firebase
) {

    public record Delivery(
            boolean enabled,
            String cron
    ) {
    }

    public record Firebase(
            String serviceAccountBase64
    ) {
    }
}
