package com.ipillgood.server.domain.notification.converter;

import com.ipillgood.server.domain.notification.dto.NotificationResponse;
import com.ipillgood.server.domain.notification.entity.MemberPushToken;

public class NotificationConverter {

    public static NotificationResponse.AppPushSetting toAppPushSetting(boolean pushEnabled) {
        return NotificationResponse.AppPushSetting.builder()
                .pushEnabled(pushEnabled)
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
}
