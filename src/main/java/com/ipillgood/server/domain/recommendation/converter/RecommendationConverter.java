package com.ipillgood.server.domain.recommendation.converter;

import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.recommendation.dto.RecommendationResponse;
import com.ipillgood.server.domain.recommendation.entity.Recommendation;
import com.ipillgood.server.domain.recommendation.entity.RecommendationItem;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RecommendationConverter {

    public static RecommendationResponse.Detail toDetail(
            Recommendation recommendation,
            List<RecommendationItem> items,
            Map<Long, List<String>> effectKeywordsByIngredientId,
            Function<String, String> imageUrlResolver
    ) {
        return RecommendationResponse.Detail.builder()
                .recommendationId(recommendation.getId())
                .status(recommendation.getStatus())
                .healthSummary(recommendation.getHealthSummary())
                .failureReason(recommendation.getFailureReason())
                .startedAt(recommendation.getStartedAt())
                .completedAt(recommendation.getCompletedAt())
                .items(toItems(items, effectKeywordsByIngredientId, imageUrlResolver))
                .build();
    }

    private static List<RecommendationResponse.Item> toItems(
            List<RecommendationItem> items,
            Map<Long, List<String>> effectKeywordsByIngredientId,
            Function<String, String> imageUrlResolver
    ) {
        return items.stream()
                .map(item -> toItem(
                        item,
                        effectKeywordsByIngredientId.getOrDefault(item.getIngredient().getId(), List.of()),
                        imageUrlResolver))
                .toList();
    }

    private static RecommendationResponse.Item toItem(
            RecommendationItem item,
            List<String> effectKeywords,
            Function<String, String> imageUrlResolver
    ) {
        Ingredient ingredient = item.getIngredient();
        return RecommendationResponse.Item.builder()
                .recommendationItemId(item.getId())
                .rankNo(item.getRankNo().intValue())
                .ingredientId(ingredient.getId())
                .ingredientName(ingredient.getName())
                .imageUrl(imageUrlResolver.apply(ingredient.getImageKey()))
                .effectKeywords(effectKeywords)
                .recommendedIntake(ingredient.getRecommendedIntake())
                .recommendedIntakeTime(ingredient.getRecommendedIntakeTime())
                .aiReason(item.getAiReason())
                .build();
    }

    public static RecommendationResponse.Retry toRetry(Recommendation recommendation) {
        return RecommendationResponse.Retry.builder()
                .recommendationId(recommendation.getId())
                .status(recommendation.getStatus())
                .startedAt(recommendation.getStartedAt())
                .build();
    }
}
