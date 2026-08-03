package com.ipillgood.server.domain.search.converter;

import com.ipillgood.server.domain.search.dto.ProductSearchResponse;
import com.ipillgood.server.domain.search.entity.MemberSearchKeyword;
import com.ipillgood.server.domain.search.repository.ProductSearchProjection;
import com.ipillgood.server.global.util.EtcProductImageKeyResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProductSearchConverter {

    public static ProductSearchResponse.ProductSearch toProductSearch(
            String keyword,
            Integer size,
            Long totalCount,
            Boolean hasNext,
            String nextCursor,
            List<ProductSearchProjection.Product> rows,
            List<ProductSearchProjection.Ingredient> ingredientRows,
            Function<String, String> toImageUrl
    ) {
        Map<Long, List<ProductSearchProjection.Ingredient>> ingredientsByProductId = groupByProductId(ingredientRows);

        List<ProductSearchResponse.ProductSearchItem> products = rows.stream()
                .map(row -> toProductSearchItem(
                        row,
                        ingredientsByProductId.getOrDefault(row.productId(), List.of()),
                        toImageUrl))
                .toList();

        return ProductSearchResponse.ProductSearch.builder()
                .keyword(keyword)
                .products(products)
                .size(size)
                .totalCount(totalCount)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    public static ProductSearchResponse.RecentSearchKeywords toRecentSearchKeywords(
            List<MemberSearchKeyword> keywords
    ) {
        List<ProductSearchResponse.RecentSearchKeyword> recentSearchKeywords = keywords.stream()
                .map(keyword -> toRecentSearchKeyword(keyword)).toList();

        return ProductSearchResponse.RecentSearchKeywords.builder()
                .keywords(recentSearchKeywords)
                .build();
    }

    private static ProductSearchResponse.ProductSearchItem toProductSearchItem(
            ProductSearchProjection.Product row,
            List<ProductSearchProjection.Ingredient> ingredients,
            Function<String, String> toImageUrl
    ) {
        List<String> ingredientNames = ingredients.stream()
                .map(ProductSearchProjection.Ingredient::ingredientName)
                .toList();
        String imageKey = ingredients.size() == 1
                ? ingredients.get(0).imageKey()
                : EtcProductImageKeyResolver.resolve(row.productId());

        return ProductSearchResponse.ProductSearchItem.builder()
                .productId(row.productId())
                .productName(row.productName())
                .brand(row.brand())
                .imageUrl(toImageUrl.apply(imageKey))
                .mfdsCertified(row.mfdsCertified())
                .ingredientNames(ingredientNames)
                .averageRating(toRoundedAverageRating(row.averageRating()))
                .reviewCount(row.reviewCount() == null ? 0 : row.reviewCount().intValue())
                .build();
    }

    public static ProductSearchResponse.RecentSearchKeyword toRecentSearchKeyword(
            MemberSearchKeyword keyword
    ) {
        return ProductSearchResponse.RecentSearchKeyword.builder()
                .keywordId(keyword.getId())
                .keyword(keyword.getKeyword())
                .searchedAt(keyword.getSearchedAt())
                .build();
    }

    public static ProductSearchResponse.DeletedKeyword toDeletedKeyword(
            MemberSearchKeyword keyword
    ) {
        return ProductSearchResponse.DeletedKeyword.builder()
                .deleted(true)
                .keywordId(keyword.getId())
                .build();
    }

    public static ProductSearchResponse.DeletedKeywords toDeletedKeywords(
            Integer deletedCount
    ){
        return ProductSearchResponse.DeletedKeywords.builder()
                .deletedCount(deletedCount)
                .build();
    }

    private static Map<Long, List<ProductSearchProjection.Ingredient>> groupByProductId(List<ProductSearchProjection.Ingredient> ingredientRows) {
        Map<Long, List<ProductSearchProjection.Ingredient>> grouped = new HashMap<>();
        for (ProductSearchProjection.Ingredient row : ingredientRows) {
            grouped.computeIfAbsent(row.productId(), productId -> new ArrayList<>()).add(row);
        }
        return grouped;
    }

    private static Double toRoundedAverageRating(Double averageRating) {
        if (averageRating == null) {
            return null;
        }
        return Math.round(averageRating * 10) / 10.0;
    }
}
