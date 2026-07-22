package com.ipillgood.server.domain.notification.dto;

import com.ipillgood.server.domain.notification.entity.enums.PushPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

public class NotificationResponse {

    @Schema(description = "앱 푸시 설정 조회 응답")
    @Builder
    public record AppPushSetting(
            @Schema(description = "앱 전체 푸시 알림 ON/OFF 여부", example = "true")
            Boolean pushEnabled
    ) {
    }

    @Schema(description = "푸시 토큰 등록 응답")
    @Builder
    public record PushTokenRegistration(
            @Schema(description = "등록 또는 갱신된 회원 푸시 토큰 ID", example = "21")
            Long pushTokenId,

            @Schema(description = "푸시 토큰 플랫폼 enum", example = "WEB")
            PushPlatform platform,

            @Schema(description = "푸시 토큰 활성 여부", example = "true")
            Boolean active,

            @Schema(description = "토큰이 마지막으로 확인된 일시", example = "2026-07-22T10:30:00")
            LocalDateTime lastSeenAt
    ) {
    }

    @Schema(description = "푸시 토큰 비활성화 응답")
    @Builder
    public record PushTokenDeactivation(
            @Schema(description = "비활성화된 회원 푸시 토큰 ID", example = "21")
            Long pushTokenId,

            @Schema(description = "푸시 토큰 활성 여부", example = "false")
            Boolean active
    ) {
    }
}
