package com.ipillgood.server.domain.notification.dto;

import com.ipillgood.server.domain.notification.entity.enums.PushPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationResponse {

    @Schema(description = "앱 푸시 설정 조회 응답")
    @Builder
    public record AppPushSetting(
            @Schema(description = "앱 전체 푸시 알림 ON/OFF 여부", example = "true")
            Boolean pushEnabled
    ) {
    }

    @Schema(description = "복용 알림 설정 통합 조회 응답")
    @Builder
    public record IntakeNotificationSettings(
            @Schema(description = "앱 전체 푸시 알림 ON/OFF 여부", example = "true")
            Boolean pushEnabled,

            @Schema(description = "복용 전체 알림 ON/OFF 여부", example = "true")
            Boolean intakePushEnabled,

            @Schema(description = "현재 섭취 중인 영양제 수", example = "2")
            Integer activeProductCount,

            @Schema(description = "현재 섭취 중인 영양제별 개별 알림 설정 목록")
            List<IntakeNotificationActiveProduct> activeProducts
    ) {
    }

    @Schema(description = "복용 전체 알림 변경 응답")
    @Builder
    public record IntakePushSetting(
            @Schema(description = "앱 전체 푸시 알림 ON/OFF 여부", example = "true")
            Boolean pushEnabled,

            @Schema(description = "복용 전체 알림 ON/OFF 여부", example = "false")
            Boolean intakePushEnabled
    ) {
    }

    @Schema(description = "복용 알림 설정 섭취 중 영양제 항목")
    @Builder
    public record IntakeNotificationActiveProduct(
            @Schema(description = "활성 섭취 중 상품 ID", example = "7")
            Long activeProductId,

            @Schema(description = "회원 캐비닛 상품 ID", example = "15")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "알림 설정 목록에 표시할 영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "개별 복용 알림 ON/OFF 여부", example = "true")
            Boolean notificationEnabled,

            @Schema(description = "복용 알림 기준 시간", example = "08:30")
            String intakeTime
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
