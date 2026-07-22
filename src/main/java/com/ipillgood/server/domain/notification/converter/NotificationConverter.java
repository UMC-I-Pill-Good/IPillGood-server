package com.ipillgood.server.domain.notification.converter;

import com.ipillgood.server.domain.notification.dto.NotificationResponse;

public class NotificationConverter {

    public static NotificationResponse.AppPushSetting toAppPushSetting(boolean pushEnabled) {
        return NotificationResponse.AppPushSetting.builder()
                .pushEnabled(pushEnabled)
                .build();
    }
}
