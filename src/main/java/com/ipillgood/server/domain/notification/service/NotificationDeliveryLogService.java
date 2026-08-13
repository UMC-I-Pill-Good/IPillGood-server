package com.ipillgood.server.domain.notification.service;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.notification.entity.MemberPushToken;
import com.ipillgood.server.domain.notification.entity.NotificationDeliveryLog;
import com.ipillgood.server.domain.notification.entity.enums.NotificationType;
import com.ipillgood.server.domain.notification.repository.MemberPushTokenRepository;
import com.ipillgood.server.domain.notification.repository.NotificationDeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryLogService {

    private final NotificationDeliveryLogRepository notificationDeliveryLogRepository;
    private final MemberPushTokenRepository memberPushTokenRepository;

    @Transactional
    public Optional<Long> createPendingLogIfAbsent(
            Member member,
            MemberPushToken pushToken,
            NotificationType notificationType,
            String title,
            String body,
            String targetRoute,
            LocalDateTime scheduledAt
    ) {
        if (notificationDeliveryLogRepository.existsByMemberPushToken_IdAndNotificationTypeAndScheduledAt(
                pushToken.getId(),
                notificationType,
                scheduledAt
        )) {
            return Optional.empty();
        }

        try {
            NotificationDeliveryLog log = NotificationDeliveryLog.createPending(
                    member,
                    pushToken,
                    notificationType,
                    title,
                    body,
                    targetRoute,
                    scheduledAt
            );
            return Optional.of(notificationDeliveryLogRepository.saveAndFlush(log).getId());
        } catch (DataIntegrityViolationException ignored) {
            return Optional.empty();
        }
    }

    @Transactional
    public void markSent(Long deliveryLogId, LocalDateTime sentAt, short retryCount) {
        notificationDeliveryLogRepository.findById(deliveryLogId)
                .ifPresent(log -> log.markSent(sentAt, retryCount));
    }

    @Transactional
    public void markInvalidTokenFailed(Long deliveryLogId, Long pushTokenId, String failureReason) {
        memberPushTokenRepository.findById(pushTokenId)
                .ifPresent(MemberPushToken::deactivate);
        notificationDeliveryLogRepository.findById(deliveryLogId)
                .ifPresent(log -> log.markFailed(failureReason));
    }

    @Transactional
    public void markRetryFailed(Long deliveryLogId, String failureReason) {
        notificationDeliveryLogRepository.findById(deliveryLogId)
                .ifPresent(log -> log.markRetryFailed(failureReason));
    }

    @Transactional
    public void markRetryFailedAndDeactivateToken(Long deliveryLogId, Long pushTokenId, String failureReason) {
        memberPushTokenRepository.findById(pushTokenId)
                .ifPresent(MemberPushToken::deactivate);
        notificationDeliveryLogRepository.findById(deliveryLogId)
                .ifPresent(log -> log.markRetryFailed(failureReason));
    }
}
