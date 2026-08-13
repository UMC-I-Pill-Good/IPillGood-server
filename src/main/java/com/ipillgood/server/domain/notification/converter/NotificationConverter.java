package com.ipillgood.server.domain.notification.converter;

import com.ipillgood.server.domain.intake.repository.IntakeNotificationActiveProductRow;
import com.ipillgood.server.domain.notification.dto.NotificationResponse;
import com.ipillgood.server.domain.notification.entity.MemberPushToken;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationConverter {

    private static final DateTimeFormatter INTAKE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static NotificationResponse.AppPushSetting toAppPushSetting(boolean pushEnabled) {
        return NotificationResponse.AppPushSetting.builder()
                .pushEnabled(pushEnabled)
                .build();
    }

    public static NotificationResponse.IntakePushSetting toIntakePushSetting(
            boolean pushEnabled,
            boolean intakePushEnabled
    ) {
        return NotificationResponse.IntakePushSetting.builder()
                .pushEnabled(pushEnabled)
                .intakePushEnabled(intakePushEnabled)
                .build();
    }

    public static NotificationResponse.ActiveProductNotificationSetting toActiveProductNotificationSetting(
            long activeProductId,
            boolean notificationEnabled
    ) {
        return NotificationResponse.ActiveProductNotificationSetting.builder()
                .activeProductId(activeProductId)
                .notificationEnabled(notificationEnabled)
                .build();
    }

    public static NotificationResponse.IntakeNotificationSettings toIntakeNotificationSettings(
            boolean pushEnabled,
            boolean intakePushEnabled,
            List<IntakeNotificationActiveProductRow> rows
    ) {
        List<NotificationResponse.IntakeNotificationActiveProduct> activeProducts = rows.stream()
                .map(NotificationConverter::toIntakeNotificationActiveProduct)
                .toList();

        return NotificationResponse.IntakeNotificationSettings.builder()
                .pushEnabled(pushEnabled)
                .intakePushEnabled(intakePushEnabled)
                .activeProductCount(activeProducts.size())
                .activeProducts(activeProducts)
                .build();
    }

    public static NotificationResponse.PushTokenRegistration toPushTokenRegistration(MemberPushToken pushToken) {
        return NotificationResponse.PushTokenRegistration.builder()
                .pushTokenId(pushToken.getId())
                .platform(pushToken.getPlatform())
                .active(pushToken.isActive())
                .lastSeenAt(pushToken.getLastSeenAt())
                .build();
    }

    public static NotificationResponse.PushTokenDeactivation toPushTokenDeactivation(MemberPushToken pushToken) {
        return NotificationResponse.PushTokenDeactivation.builder()
                .pushTokenId(pushToken.getId())
                .active(pushToken.isActive())
                .build();
    }

    private static NotificationResponse.IntakeNotificationActiveProduct toIntakeNotificationActiveProduct(
            IntakeNotificationActiveProductRow row
    ) {
        return NotificationResponse.IntakeNotificationActiveProduct.builder()
                .activeProductId(row.activeProductId())
                .memberProductId(row.memberProductId())
                .productId(row.productId())
                .productName(row.productName())
                .notificationEnabled(row.notificationEnabled())
                .intakeTime(row.intakeTime().format(INTAKE_TIME_FORMATTER))
                .build();
    }
}
