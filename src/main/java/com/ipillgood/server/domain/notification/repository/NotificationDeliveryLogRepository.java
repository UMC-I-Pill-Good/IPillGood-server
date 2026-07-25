package com.ipillgood.server.domain.notification.repository;

import com.ipillgood.server.domain.notification.entity.NotificationDeliveryLog;
import com.ipillgood.server.domain.notification.entity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, Long> {

    boolean existsByMemberPushToken_IdAndNotificationTypeAndScheduledAt(
            Long memberPushTokenId,
            NotificationType notificationType,
            LocalDateTime scheduledAt
    );
}
