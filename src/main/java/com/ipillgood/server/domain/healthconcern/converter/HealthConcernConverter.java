package com.ipillgood.server.domain.healthconcern.converter;

import com.ipillgood.server.domain.healthconcern.dto.HealthConcernResponse;
import com.ipillgood.server.domain.healthconcern.entity.HealthConcern;
import com.ipillgood.server.domain.healthconcern.entity.HealthConcernIngredient;
import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.healthconcern.entity.enums.MinorCategory;
import com.ipillgood.server.domain.ingredient.entity.EffectKeyword;
import com.ipillgood.server.domain.ingredient.entity.Ingredient;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HealthConcernConverter {

    private HealthConcernConverter() {
    }

    public static HealthConcernResponse.CategoryList toCategoryList() {
        var minorCategoriesByMajor = Arrays.stream(MinorCategory.values())
                .collect(Collectors.groupingBy(MinorCategory::getMajorCategory));

        List<HealthConcernResponse.MajorCategoryGroup> majorCategories = Arrays.stream(MajorCategory.values())
                .map(majorCategory -> toMajorCategoryGroup(
                        majorCategory,
                        minorCategoriesByMajor.getOrDefault(majorCategory, List.of())))
                .toList();

        return HealthConcernResponse.CategoryList.builder()
                .majorCategories(majorCategories)
                .build();
    }

    private static HealthConcernResponse.MajorCategoryGroup toMajorCategoryGroup(
            MajorCategory majorCategory,
            List<MinorCategory> minorCategories
    ) {
        return HealthConcernResponse.MajorCategoryGroup.builder()
                .type(majorCategory)
                .label(majorCategory.getLabel())
                .minorCategories(minorCategories.stream()
                        .map(HealthConcernConverter::toMinorCategoryItem)
                        .toList())
                .build();
    }

    private static HealthConcernResponse.MinorCategoryItem toMinorCategoryItem(MinorCategory minorCategory) {
        return HealthConcernResponse.MinorCategoryItem.builder()
                .type(minorCategory)
                .label(minorCategory.getLabel())
                .build();
    }

    public static HealthConcernResponse.RecommendedIngredients toRecommendedIngredients(
            HealthConcern healthConcern,
            List<HealthConcernIngredient> healthConcernIngredients,
            List<EffectKeyword> effectKeywords,
            Set<Long> cabinetIngredientIds,
            Function<String, String> imageUrlResolver
    ) {
        Map<Long, List<String>> keywordsByIngredientId = effectKeywords.stream()
                .collect(Collectors.groupingBy(
                        effectKeyword -> effectKeyword.getIngredient().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(EffectKeyword::getKeyword, Collectors.toList())
                ));

        List<HealthConcernResponse.RecommendedIngredient> recommendedIngredients = healthConcernIngredients.stream()
                .map(healthConcernIngredient -> toRecommendedIngredient(
                        healthConcernIngredient.getIngredient(),
                        keywordsByIngredientId,
                        cabinetIngredientIds,
                        imageUrlResolver))
                .toList();

        return HealthConcernResponse.RecommendedIngredients.builder()
                .healthConcernId(healthConcern.getId())
                .majorCategory(healthConcern.getMajorCategory())
                .minorCategory(healthConcern.getMinorCategory())
                .declineCause(healthConcern.getDeclineCause())
                .recommendedIngredients(recommendedIngredients)
                .build();
    }

    private static HealthConcernResponse.RecommendedIngredient toRecommendedIngredient(
            Ingredient ingredient,
            Map<Long, List<String>> keywordsByIngredientId,
            Set<Long> cabinetIngredientIds,
            Function<String, String> imageUrlResolver
    ) {
        return HealthConcernResponse.RecommendedIngredient.builder()
                .ingredientId(ingredient.getId())
                .name(ingredient.getName())
                .description(ingredient.getDescription())
                .imageUrl(imageUrlResolver.apply(ingredient.getImageKey()))
                .effectKeywords(keywordsByIngredientId.getOrDefault(ingredient.getId(), List.of()))
                .hasCabinetProduct(cabinetIngredientIds.contains(ingredient.getId()))
                .build();
    }
}
