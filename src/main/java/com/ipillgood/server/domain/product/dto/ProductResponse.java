package com.ipillgood.server.domain.product.dto;

import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
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

            @Schema(description = "구매처 검색 링크 URL. 상품명으로 쿠팡 검색 결과 페이지를 여는 주소이며, "
                    + "특정 판매 상품 페이지가 아닙니다.",
                    example = "https://www.coupang.com/np/search?q=%EC%95%84%EC%9D%B4%ED%95%84%EA%B5%BF"
                            + "+%EB%A9%94%EA%B0%80%EB%8F%84%EC%8A%A4+%EB%B9%84%ED%83%80%EB%AF%BCC+1000")
            String purchaseUrl,

            @Schema(description = "식약처 인증 여부", example = "true")
            Boolean mfdsCertified,

            @Schema(description = "평균 별점(후기 없으면 0)", example = "4.5")
            Double ratingAverage,

            @Schema(description = "후기 수", example = "128")
            Integer reviewCount,

            @Schema(description = "로그인 회원이 이미 캐비닛에 보유 중인 상품인지 여부", example = "true")
            Boolean isOwned,

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

    @Schema(description = "상품 추가 시 주의 조합 확인 응답 (캐비닛 보유 성분 기준)")
    @Builder
    public record ProductPurchaseCautionCheck(
            @Schema(description = "확인 대상 상품 ID", example = "1")
            Long productId,

            @Schema(description = "상품 구매처 검색 링크 URL. 상품명으로 쿠팡 검색 결과 페이지를 여는 주소이며, "
                    + "특정 판매 상품 페이지가 아닙니다.",
                    example = "https://www.coupang.com/np/search?q=%EC%95%84%EC%9D%B4%ED%95%84%EA%B5%BF"
                            + "+%EB%A9%94%EA%B0%80%EB%8F%84%EC%8A%A4+%EB%B9%84%ED%83%80%EB%AF%BCC+1000")
            String purchaseUrl,

            @Schema(description = "보유 성분과 주의 조합이 하나라도 있는지 여부", example = "true")
            Boolean hasConflict,

            @Schema(description = "주의가 필요한 조합 목록(없으면 빈 배열)")
            List<CautionCombination> conflicts
    ) {

        @Schema(description = "보유 성분 ↔ 상품 성분 간 주의 조합 항목")
        @Builder
        public record CautionCombination(
                @Schema(description = "조합 유형", example = "CAUTION")
                CombinationType type,

                @Schema(description = "사용자가 보유한(섭취 중인) 성분 ID", example = "3")
                Long currentIngredientId,

                @Schema(description = "사용자가 보유한(섭취 중인) 성분명", example = "종합비타민")
                String currentIngredientName,

                @Schema(description = "추가하려는 상품에 포함된 성분 ID", example = "7")
                Long purchaseProductIngredientId,

                @Schema(description = "추가하려는 상품에 포함된 성분명", example = "철분")
                String purchaseIngredientName,

                @Schema(description = "함께 복용 시 주의 사유", example = "철분 과다 섭취 위험, 위장 장애, 변비 유발 가능")
                String reason
        ) {}
    }
}
