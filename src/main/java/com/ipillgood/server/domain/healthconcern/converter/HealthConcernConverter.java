package com.ipillgood.server.domain.healthconcern.converter;

import com.ipillgood.server.domain.healthconcern.dto.HealthConcernResponse;
import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.healthconcern.entity.enums.MinorCategory;

import java.util.Arrays;
import java.util.List;
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
}
