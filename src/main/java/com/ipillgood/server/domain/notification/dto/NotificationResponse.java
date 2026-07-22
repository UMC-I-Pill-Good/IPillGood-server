package com.ipillgood.server.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public class NotificationResponse {

    @Schema(description = "앱 푸시 설정 조회 응답")
    @Builder
    public record AppPushSetting(
            @Schema(description = "앱 전체 푸시 알림 ON/OFF 여부", example = "true")
            Boolean pushEnabled
    ) {
    }
}
