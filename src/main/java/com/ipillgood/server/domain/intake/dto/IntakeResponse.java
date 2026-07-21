package com.ipillgood.server.domain.intake.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class IntakeResponse {

    @Schema(description = "섭취 중 영양제 목록 조회 응답")
    @Builder
    public record ActiveProducts(
            @Schema(description = "현재 섭취 중인 영양제 수", example = "2")
            Integer totalCount,

            @Schema(description = "현재 섭취 중인 영양제 목록")
            List<ActiveProductSummary> activeProducts
    ) {
    }

    @Schema(description = "섭취 중 영양제 카드 항목")
    @Builder
    public record ActiveProductSummary(
            @Schema(description = "활성 섭취 중 상품 ID", example = "7")
            Long activeProductId,

            @Schema(description = "회원 캐비닛 상품 ID", example = "15")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "카드에 표시할 썸네일 이미지 URL")
            String thumbnailImageUrl
    ) {
    }
}

