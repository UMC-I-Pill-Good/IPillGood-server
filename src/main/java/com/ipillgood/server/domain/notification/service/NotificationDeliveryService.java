package com.ipillgood.server.domain.notification.service;

import com.ipillgood.server.domain.intake.repository.IntakeNotificationDeliveryProductRow;
import com.ipillgood.server.domain.intake.repository.MemberActiveProductRepository;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.notification.entity.MemberPushToken;
import com.ipillgood.server.domain.notification.entity.enums.NotificationType;
import com.ipillgood.server.domain.notification.repository.MemberPushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String INTAKE_TARGET_ROUTE = "/home";
    private static final String CONDITION_CHECK_TARGET_ROUTE = "/condition";
    private static final String PUSH_NOTIFICATION_TITLE = "아필굿";
    private static final String CONDITION_CHECK_BODY = "이번 주 컨디션을 기록할 시간이에요.";
    private static final int MAX_BODY_LENGTH = 500;

    private final MemberActiveProductRepository memberActiveProductRepository;
    private final MemberPushTokenRepository memberPushTokenRepository;
    private final NotificationDeliveryLogService notificationDeliveryLogService;
    private final PushNotificationClient pushNotificationClient;

    public void deliverDueNotifications(LocalDateTime currentDateTime) {
        LocalDateTime scheduledAt = currentDateTime.truncatedTo(ChronoUnit.MINUTES);
        deliverIntakeNotifications(scheduledAt);
        deliverConditionCheckNotifications(scheduledAt);
    }

    public void deliverIntakeNotifications(LocalDateTime scheduledAt) {
        LocalDate currentDate = scheduledAt.toLocalDate();
        LocalTime currentTime = scheduledAt.toLocalTime();
        List<IntakeNotificationDeliveryProductRow> scheduledRows = memberActiveProductRepository
                .findIntakeNotificationDeliveryProductRows(currentDate, currentTime)
                .stream()
                .filter(row -> isScheduledOn(row, currentDate))
                .toList();
        if (scheduledRows.isEmpty()) {
            return;
        }

        Map<Long, List<IntakeNotificationDeliveryProductRow>> rowsByMemberId = scheduledRows.stream()
                .collect(Collectors.groupingBy(
                        IntakeNotificationDeliveryProductRow::memberId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        Map<Long, List<MemberPushToken>> tokensByMemberId = findActiveTokensByMemberId(rowsByMemberId.keySet());

        for (Map.Entry<Long, List<IntakeNotificationDeliveryProductRow>> entry : rowsByMemberId.entrySet()) {
            List<MemberPushToken> pushTokens = tokensByMemberId.get(entry.getKey());
            if (pushTokens == null || pushTokens.isEmpty()) {
                continue;
            }

            String body = buildIntakeBody(currentTime, entry.getValue());
            PushNotificationPayload payload = new PushNotificationPayload(
                    NotificationType.INTAKE,
                    PUSH_NOTIFICATION_TITLE,
                    body,
                    INTAKE_TARGET_ROUTE
            );
            sendToTokens(pushTokens, payload, scheduledAt);
        }
    }

    public void deliverConditionCheckNotifications(LocalDateTime scheduledAt) {
        LocalDate currentDate = scheduledAt.toLocalDate();
        LocalTime currentTime = scheduledAt.toLocalTime();
        if (currentDate.getDayOfWeek() != DayOfWeek.SUNDAY || !isConditionCheckDeliveryTime(currentTime)) {
            return;
        }

        LocalDate weekStartOn = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<MemberPushToken> pushTokens = memberPushTokenRepository.findConditionCheckDeliveryTokens(weekStartOn);
        PushNotificationPayload payload = new PushNotificationPayload(
                NotificationType.CONDITION_CHECK,
                PUSH_NOTIFICATION_TITLE,
                CONDITION_CHECK_BODY,
                CONDITION_CHECK_TARGET_ROUTE
        );
        sendToTokens(pushTokens, payload, scheduledAt);
    }

    private Map<Long, List<MemberPushToken>> findActiveTokensByMemberId(Collection<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Map.of();
        }

        return memberPushTokenRepository.findActiveTokensByMemberIds(memberIds)
                .stream()
                .collect(Collectors.groupingBy(
                        token -> token.getMember().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private void sendToTokens(
            List<MemberPushToken> pushTokens,
            PushNotificationPayload payload,
            LocalDateTime scheduledAt
    ) {
        for (MemberPushToken pushToken : pushTokens) {
            sendToToken(pushToken, payload, scheduledAt);
        }
    }

    private void sendToToken(
            MemberPushToken pushToken,
            PushNotificationPayload payload,
            LocalDateTime scheduledAt
    ) {
        Member member = pushToken.getMember();
        Optional<Long> deliveryLogId = notificationDeliveryLogService.createPendingLogIfAbsent(
                member,
                pushToken,
                payload.notificationType(),
                payload.title(),
                payload.body(),
                payload.targetRoute(),
                scheduledAt
        );
        if (deliveryLogId.isEmpty()) {
            return;
        }

        PushSendResult firstResult = pushNotificationClient.send(pushToken.getToken(), payload);
        if (firstResult.successful()) {
            markSent(deliveryLogId.get(), (short) 0);
            return;
        }
        if (firstResult.invalidToken()) {
            notificationDeliveryLogService.markInvalidTokenFailed(
                    deliveryLogId.get(),
                    pushToken.getId(),
                    firstResult.failureReason()
            );
            return;
        }

        PushSendResult retryResult = pushNotificationClient.send(pushToken.getToken(), payload);
        if (retryResult.successful()) {
            markSent(deliveryLogId.get(), (short) 1);
        } else if (retryResult.invalidToken()) {
            notificationDeliveryLogService.markRetryFailedAndDeactivateToken(
                    deliveryLogId.get(),
                    pushToken.getId(),
                    retryResult.failureReason()
            );
        } else {
            notificationDeliveryLogService.markRetryFailed(deliveryLogId.get(), retryResult.failureReason());
        }
    }

    private void markSent(Long deliveryLogId, short retryCount) {
        notificationDeliveryLogService.markSent(
                deliveryLogId,
                LocalDateTime.now(SERVICE_ZONE_ID).truncatedTo(ChronoUnit.SECONDS),
                retryCount
        );
    }

    private boolean isScheduledOn(IntakeNotificationDeliveryProductRow row, LocalDate currentDate) {
        if (row.scheduleAnchorOn() == null || row.frequencyIntervalDays() == null
                || row.frequencyIntervalDays() < 1) {
            return false;
        }

        long daysSinceAnchor = ChronoUnit.DAYS.between(row.scheduleAnchorOn(), currentDate);
        return daysSinceAnchor >= 0 && daysSinceAnchor % row.frequencyIntervalDays() == 0;
    }

    private boolean isConditionCheckDeliveryTime(LocalTime currentTime) {
        return LocalTime.NOON.equals(currentTime) || LocalTime.of(21, 0).equals(currentTime);
    }

    private String buildIntakeBody(
            LocalTime intakeTime,
            List<IntakeNotificationDeliveryProductRow> productRows
    ) {
        List<String> productNames = productRows.stream()
                .map(IntakeNotificationDeliveryProductRow::productName)
                .toList();
        String productPart = buildProductPart(productNames);
        String prefix = formatIntakeTime(intakeTime) + ", 건강 루틴 지킬 시간!💊 ";
        String suffix = " 오늘도 챙겨봐요🍃";

        int maxProductPartLength = MAX_BODY_LENGTH - prefix.length() - suffix.length();
        if (maxProductPartLength < productPart.length()) {
            productPart = truncate(productPart, Math.max(maxProductPartLength, 0));
        }
        return prefix + productPart + suffix;
    }

    private String buildProductPart(List<String> productNames) {
        if (productNames.size() <= 3) {
            return String.join(", ", productNames);
        }

        String productNamePart = String.join(", ", productNames.subList(0, 3));
        return productNamePart + " 외 " + (productNames.size() - 3) + "개";
    }

    private String formatIntakeTime(LocalTime intakeTime) {
        if (intakeTime.getMinute() == 0) {
            return intakeTime.getHour() + "시";
        }
        return intakeTime.getHour() + "시 " + intakeTime.getMinute() + "분";
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
