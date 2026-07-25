package com.ipillgood.server.domain.search.converter;

import com.ipillgood.server.domain.search.dto.ProductSearchResponse;
import com.ipillgood.server.domain.search.entity.MemberSearchKeyword;
import com.ipillgood.server.domain.search.repository.ProductSearchProjection;
import com.ipillgood.server.global.s3.S3Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ProductSearchConverter {

    private static final List<String> MULTI_INGREDIENT_THUMBNAIL_KEYS = List.of(
            "ingredients/other1.png",
            "ingredients/other2.png",
            "ingredients/other3.png",
            "ingredients/other4.png"
    );

    public static ProductSearchResponse.ProductSearch toProductSearch(
            String keyword,
            Integer size,
            Long totalCount,
            Boolean hasNext,
            String nextCursor,
            List<ProductSearchProjection.Product> rows,
            List<ProductSearchProjection.Ingredient> ingredientRows,
            S3Service s3Service
    ) {
        Map<Long, List<ProductSearchProjection.Ingredient>> ingredientsByProductId = groupByProductId(ingredientRows);

        List<ProductSearchResponse.ProductSearchItem> products = rows.stream()
                .map(row -> toProductSearchItem(
                        row,
                        ingredientsByProductId.getOrDefault(row.productId(), List.of()),
                        s3Service))
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
        List<ProductSearchResponse.RecentSearchKeywords.RecentSearchKeyword> recentSearchKeywords = keywords.stream()
                .map(keyword -> toRecentSearchKeyword(keyword)).toList();

        return ProductSearchResponse.RecentSearchKeywords.builder()
                .keywords(recentSearchKeywords)
                .build();
    }

    private static ProductSearchResponse.ProductSearchItem toProductSearchItem(
            ProductSearchProjection.Product row,
            List<ProductSearchProjection.Ingredient> ingredients,
            S3Service s3Service
    ) {
        List<String> ingredientNames = ingredients.stream()
                .map(ProductSearchProjection.Ingredient::ingredientName)
                .toList();

        return ProductSearchResponse.ProductSearchItem.builder()
                .productId(row.productId())
                .productName(row.productName())
                .brand(row.brand())
                .imageUrl(toImageUrl(ingredients, s3Service))
                .mfdsCertified(row.mfdsCertified())
                .ingredientNames(ingredientNames)
                .averageRating(toRoundedAverageRating(row.averageRating()))
                .reviewCount(row.reviewCount() == null ? 0 : row.reviewCount().intValue())
                .build();
    }

    private static ProductSearchResponse.RecentSearchKeywords.RecentSearchKeyword toRecentSearchKeyword(
            MemberSearchKeyword keyword
    ) {
        return ProductSearchResponse.RecentSearchKeywords.RecentSearchKeyword.builder()
                .keywordId(keyword.getId())
                .keyword(keyword.getKeyword())
                .searchedAt(keyword.getSearchedAt())
                .build();
    }

    private static String toImageUrl(List<ProductSearchProjection.Ingredient> ingredients, S3Service s3Service) {
        String imageKey = ingredients.size() == 1
                ? ingredients.get(0).imageKey()
                : randomMultiIngredientImageKey();
        return s3Service.getPublicUrl(imageKey);
    }

    private static String randomMultiIngredientImageKey() {
        int index = ThreadLocalRandom.current().nextInt(MULTI_INGREDIENT_THUMBNAIL_KEYS.size());
        return MULTI_INGREDIENT_THUMBNAIL_KEYS.get(index);
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
