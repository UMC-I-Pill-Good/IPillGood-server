package com.ipillgood.server.domain.cabinet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class CabinetResponse {

    @Schema(description = "캐비닛 영양제 추가 응답")
    @Builder
    public record AddProducts(
            @Schema(description = "추가된 영양제 수", example = "2")
            Integer addedCount,

            @Schema(description = "추가된 캐비닛 보유 영양제 목록")
            List<AddedProduct> addedProducts
    ) {
    }

    @Schema(description = "추가된 캐비닛 보유 영양제 항목")
    @Builder
    public record AddedProduct(
            @Schema(description = "생성된 회원 캐비닛 상품 ID", example = "21")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "118")
            Long productId,

            @Schema(description = "브랜드명", example = "솔가")
            String brand,

            @Schema(description = "영양제 상품명", example = "솔가 비타민D3 1000IU")
            String productName,

            @Schema(description = "카드에 표시할 썸네일 이미지 URL")
            String thumbnailImageUrl,

            @Schema(description = "캐비닛에 추가한 일시", example = "2026-07-21T11:30:00")
            LocalDateTime addedAt
    ) {
    }

    @Schema(description = "캐비닛 보유 영양제 목록 조회 응답")
    @Builder
    public record ProductList(
            @Schema(description = "상단 안내 문구에 사용할 회원 닉네임", example = "필굿")
            String memberNickname,

            @Schema(description = "캐비닛 보유 영양제 수", example = "2")
            Integer totalCount,

            @Schema(description = "캐비닛 보유 영양제 목록")
            List<ProductSummary> products
    ) {
    }

    @Schema(description = "캐비닛 보유 영양제 요약 항목")
    @Builder
    public record ProductSummary(
            @Schema(description = "회원 캐비닛 상품 ID", example = "15")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "카드에 표시할 썸네일 이미지 URL")
            String thumbnailImageUrl,

            @Schema(description = "현재 섭취 중인 영양제로 등록되어 있는지 여부", example = "true")
            Boolean isActiveIntake,

            @Schema(description = "활성 섭취 중 상품 ID", nullable = true, example = "7")
            Long activeProductId,

            @Schema(description = "캐비닛에 추가한 일시", example = "2026-07-01T10:20:00")
            LocalDateTime addedAt
    ) {
    }
}
