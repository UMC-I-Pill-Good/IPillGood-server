package com.ipillgood.server.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class ProductSearchResponse {

    @Schema(description = "영양제 상품 목록 조회 응답")
    @Builder
    public record ProductSearch(
            @Schema(description = "조회에 적용된 검색어. 검색어 없이 기본 목록 조회 시 null", nullable = true, example = "비타민")
            String keyword,

            @Schema(description = "영양제 상품 목록")
            List<ProductSearchItem> products,

            @Schema(description = "페이지 크기", example = "20")
            Integer size,

            @Schema(description = "검색 조건에 맞는 전체 상품 수", example = "42")
            Long totalCount,

            @Schema(description = "다음 페이지 존재 여부", example = "true")
            Boolean hasNext,

            @Schema(description = "다음 커서. hasNext가 false면 null", nullable = true, example = "101|12")
            String nextCursor
    ) {
    }

    @Schema(description = "영양제 상품 목록 항목")
    @Builder
    public record ProductSearchItem(
            @Schema(description = "영양제 상품 ID", example = "101")
            Long productId,

            @Schema(description = "영양제 상품명", example = "알티지 오메가3")
            String productName,

            @Schema(description = "브랜드명", example = "종근당건강")
            String brand,

            @Schema(description = "카드에 표시할 썸네일 이미지 URL", nullable = true)
            String imageUrl,

            @Schema(description = "식약처 인증 여부", example = "true")
            Boolean mfdsCertified,

            @Schema(description = "포함 성분명 목록", example = "[\"비타민 D\", \"칼슘\"]")
            List<String> ingredientNames,

            @Schema(description = "평균 별점(소수 둘째 자리에서 반올림). 후기가 없으면 null",
                    nullable = true, example = "4.5")
            Double averageRating,

            @Schema(description = "후기 수", example = "12")
            Integer reviewCount
    ) {
    }

    @Schema(description = "최근 검색어 조회 응답")
    @Builder
    public record RecentSearchKeywords(
            @Schema(description = "최근 검색어 목록 (최신순, 최대 10개)")
            List<RecentSearchKeyword> keywords
    ) {}

    @Schema(description = "최근 검색어 항목")
    @Builder
    public record RecentSearchKeyword(
            @Schema(description = "최근 검색어 ID", example = "1")
            Long keywordId,

            @Schema(description = "검색어", example = "비타민")
            String keyword,

            @Schema(description = "검색 일시", example = "2026-07-20T15:00:00")
            LocalDateTime searchedAt
    ) {}

    @Schema(description = "최근 검색어 삭제 응답")
    @Builder
    public record DeletedKeyword(
            @Schema(description = "삭제 여부", example = "true")
            Boolean deleted,

            @Schema(description = "삭제된 최근 검색어 ID", example = "1")
            Long keywordId
    ) {}

    @Schema(description = "최근 검색어 전체 삭제 응답")
    @Builder
    public record DeletedKeywords(
            @Schema(description = "삭제된 최근 검색어 수", example = "10")
            Integer deletedCount
    ) {}
}
