package com.ipillgood.server.domain.intake.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class IntakeRequest {

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

    @Schema(description = "섭취 중 등록 전 병용 금기 확인 요청")
    public record CompatibilityCheck(
            @Schema(description = "섭취 중으로 등록하려는 회원 캐비닛 상품 ID", example = "16")
            Long memberProductId
    ) {
    }
}

