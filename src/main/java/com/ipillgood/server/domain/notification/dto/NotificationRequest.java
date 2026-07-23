package com.ipillgood.server.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class NotificationRequest {

    @Schema(description = "앱 푸시 설정 변경 요청")
    public record UpdateAppPushSetting(
            @Schema(description = "변경할 앱 전체 푸시 알림 ON/OFF 여부", type = "boolean", example = "false")
            Object pushEnabled
    ) {
    }

    @Schema(description = "복용 전체 알림 변경 요청")
    public record UpdateIntakePushSetting(
            @Schema(description = "변경할 복용 전체 알림 ON/OFF 여부", type = "boolean", example = "false")
            Object intakePushEnabled
    ) {
    }

    @Schema(description = "푸시 토큰 등록 요청")
    public record RegisterPushToken(
            @Schema(description = "푸시 토큰 플랫폼 enum", type = "string", example = "WEB")
            Object platform,

            @Schema(description = "웹앱에서 발급받은 FCM 등록 토큰", type = "string", example = "fcm_registration_token_sample")
            Object token
    ) {
    }
}
