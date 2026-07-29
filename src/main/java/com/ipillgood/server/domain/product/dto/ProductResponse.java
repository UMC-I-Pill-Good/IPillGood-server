package com.ipillgood.server.domain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class ProductResponse {

    @Schema(description = "영양제 상품 상세 조회 응답")
    @Builder
    public record ProductInfo(
            @Schema(description = "영양제 상품 ID", example = "1")
            Long productId,

            @Schema(description = "영양제 상품명", example = "메가도스 비타민C 1000")
            String productName,

            @Schema(description = "브랜드명", example = "아이필굿")
            String brand,

            @Schema(description = "대표 이미지 URL. 성분 1개면 해당 성분 이미지, 2개 이상이면 기타 대표 이미지",
                    example = "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/1.png")
            String imageUrl,

            @Schema(description = "상품 설명", example = "고함량 비타민C로 항산화와 면역에 도움을 주는 영양제입니다.")
            String description,

            @Schema(description = "구매 링크 URL",
                    example = "https://smartstore.naver.com/ipillgood/products/123456")
            String purchaseUrl,

            @Schema(description = "식약처 인증 여부", example = "true")
            Boolean mfdsCertified,

            @Schema(description = "평균 별점(후기 없으면 0)", example = "4.5")
            Double ratingAverage,

            @Schema(description = "후기 수", example = "128")
            Integer reviewCount,

            @Schema(description = "과대광고 위험 성분 포함 여부", example = "true")
            Boolean adClaimRisk,

            @Schema(description = "과대광고 위험 성분명 목록(없으면 빈 배열)", example = "[\"비타민C\"]")
            List<String> adClaimRiskIngredients
    ) {}

    @Schema(description = "상품 성분 정보 조회 응답")
    @Builder
    public record ProductIngredientsInfo(
            @Schema(description = "영양제 상품 ID", example = "1")
            Long productId,

            @Schema(description = "포함 성분 수", example = "2")
            Integer ingredientCount,

            @Schema(description = "성분 정보 목록")
            List<ProductIngredientInfo> ingredientInfos
    ) {

        @Schema(description = "성분 정보 항목")
        @Builder
        public record ProductIngredientInfo(
                @Schema(description = "성분 ID", example = "2")
                Long ingredientId,

                @Schema(description = "성분명", example = "비타민 D")
                String name,

                @Schema(description = "성분 설명", example = "칼슘 흡수와 뼈 건강에 도움을 주는 성분입니다.")
                String description,

                @Schema(description = "성분 이미지 URL",
                        example = "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png")
                String imageUrl,

                @Schema(description = "효능 키워드 목록(없으면 빈 배열)", example = "[\"뼈 건강\", \"면역\"]")
                List<String> effectKeywords
        ) {}
    }

    @Schema(description = "상품 성분 궁합 조회 응답 (캐비닛 보유 성분 기준)")
    @Builder
    public record ProductCombinations(
            @Schema(description = "영양제 상품 ID", example = "1")
            Long productId,

            @Schema(description = "사용자가 캐비닛에 보유한 영양제 수('보유 중인 영양제 N개 기준'에 사용)", example = "6")
            Long ownedProductCount,

            @Schema(description = "함께 섭취하면 좋은 조합 성분 목록(보유 성분 기준, 없으면 빈 배열)")
            List<ProductCombination> goodCombinations,

            @Schema(description = "주의가 필요한 조합 성분 목록(보유 성분 기준, 없으면 빈 배열)")
            List<ProductCombination> cautionCombinations
    ) {

        @Schema(description = "궁합 상대 성분 항목")
        @Builder
        public record ProductCombination(
                @Schema(description = "궁합 상대 성분 ID", example = "2")
                Long targetIngredientId,

                @Schema(description = "궁합 상대 성분명", example = "비타민 D")
                String targetIngredientName
        ) {}
    }
}
