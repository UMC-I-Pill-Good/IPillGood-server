package com.ipillgood.server.domain.healthconcern.service;

import com.ipillgood.server.domain.healthconcern.converter.HealthConcernConverter;
import com.ipillgood.server.domain.healthconcern.dto.HealthConcernResponse;
import com.ipillgood.server.domain.healthconcern.entity.HealthConcern;
import com.ipillgood.server.domain.healthconcern.entity.HealthConcernIngredient;
import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.healthconcern.entity.enums.MinorCategory;
import com.ipillgood.server.domain.healthconcern.repository.HealthConcernIngredientRepository;
import com.ipillgood.server.domain.healthconcern.repository.HealthConcernRepository;
import com.ipillgood.server.domain.ingredient.entity.EffectKeyword;
import com.ipillgood.server.domain.ingredient.repository.EffectKeywordRepository;
import com.ipillgood.server.domain.ingredient.repository.IngredientRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import com.ipillgood.server.global.s3.S3Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthConcernService {

    private final HealthConcernRepository healthConcernRepository;
    private final HealthConcernIngredientRepository healthConcernIngredientRepository;
    private final EffectKeywordRepository effectKeywordRepository;
    private final IngredientRepository ingredientRepository;
    private final S3Service s3Service;

    public HealthConcernResponse.CategoryList getCategories() {
        return HealthConcernConverter.toCategoryList();
    }

    public HealthConcernResponse.RecommendedIngredients getRecommendedIngredients(
            String majorCategoryValue,
            String minorCategoryValue,
            Long memberId
    ) {
        MajorCategory majorCategory = parseMajorCategory(majorCategoryValue);
        MinorCategory minorCategory = parseMinorCategory(minorCategoryValue);

        if (minorCategory.getMajorCategory() != majorCategory) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }

        HealthConcern healthConcern = healthConcernRepository
                .findByMajorCategoryAndMinorCategory(majorCategory, minorCategory)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.VALID_FAIL));

        List<HealthConcernIngredient> healthConcernIngredients =
                healthConcernIngredientRepository.findByHealthConcernIdOrderByIdAsc(healthConcern.getId());

        List<Long> ingredientIds = healthConcernIngredients.stream()
                .map(healthConcernIngredient -> healthConcernIngredient.getIngredient().getId())
                .toList();

        List<EffectKeyword> effectKeywords =
                effectKeywordRepository.findByIngredient_IdInOrderByIdAsc(ingredientIds);

        Set<Long> cabinetIngredientIds = ingredientIds.isEmpty()
                ? Set.of()
                : new HashSet<>(ingredientRepository.findCabinetIngredientIds(memberId, ingredientIds));

        return HealthConcernConverter.toRecommendedIngredients(
                healthConcern,
                healthConcernIngredients,
                effectKeywords,
                cabinetIngredientIds,
                s3Service::getPublicUrl
        );
    }

    private MajorCategory parseMajorCategory(String value) {
        if (value == null || value.isBlank()) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
        try {
            return MajorCategory.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
    }

    private MinorCategory parseMinorCategory(String value) {
        if (value == null || value.isBlank()) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
        try {
            return MinorCategory.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
    }
}
