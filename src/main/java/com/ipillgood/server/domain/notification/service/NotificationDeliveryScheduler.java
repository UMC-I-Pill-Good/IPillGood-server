package com.ipillgood.server.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.delivery", name = "enabled", havingValue = "true")
public class NotificationDeliveryScheduler {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final NotificationDeliveryService notificationDeliveryService;

    @Scheduled(cron = "${notification.delivery.cron}", zone = "Asia/Seoul")
    public void deliverDueNotifications() {
        notificationDeliveryService.deliverDueNotifications(LocalDateTime.now(SERVICE_ZONE_ID));
    }
}
