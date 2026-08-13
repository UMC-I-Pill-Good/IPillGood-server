package com.ipillgood.server.domain.intake.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class IntakeRequest {

    @Schema(description = "오늘 복용 체크 저장 요청")
    public record SaveTodayIntakeRecords(
            @Schema(description = "오늘 섭취 완료로 저장할 활성 섭취 중 상품 ID 목록", example = "[7]")
            List<Long> takenActiveProductIds
    ) {
    }

    @Schema(description = "섭취 중 영양제 등록 요청")
    public record RegisterActiveProduct(
            @Schema(description = "섭취 중으로 등록할 회원 캐비닛 상품 ID", example = "16")
            Long memberProductId,

            @Schema(description = "복용 시간", example = "08:30")
            String intakeTime,

            @Schema(description = "복용 주기 enum", example = "EVERY_DAY")
            String frequency
    ) {
    }

    @Schema(description = "섭취 중 영양제 설정 변경 요청")
    public record UpdateActiveProductSettings(
            @Schema(description = "변경할 복용 시간", example = "21:00")
            String intakeTime,

            @Schema(description = "변경할 복용 주기 enum", example = "EVERY_2_DAYS")
            String frequency,

            @Schema(description = "개별 복용 알림 ON/OFF 여부", example = "false")
            Boolean notificationEnabled
    ) {
    }
}

