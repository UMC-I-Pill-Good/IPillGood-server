package com.ipillgood.server.domain.search.repository;

import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.ingredient.entity.enums.TargetGender;
import com.ipillgood.server.domain.search.entity.enums.ProductSearchSort;
import com.ipillgood.server.global.enums.AgeGroup;

import java.util.List;

public record ProductSearchCondition(
        String keyword,
        ProductSearchSort sort,
        List<AgeGroup> ageGroups,
        List<TargetGender> targetGenders,
        boolean mfdsCertifiedOnly,
        List<MajorCategory> majorCategories,
        int size,
        Cursor cursor
) {

    public record Cursor(
            Long productId,
            Long reviewCount,
            Double rating,
            boolean ratingMissing
    ) {
    }
}
