package com.ipillgood.server.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class NotificationRequest {

    @Schema(description = "앱 푸시 설정 변경 요청")
    public record UpdateAppPushSetting(
            @Schema(description = "변경할 앱 전체 푸시 알림 ON/OFF 여부", type = "boolean", example = "false")
            Object pushEnabled
    ) {
    }
}
