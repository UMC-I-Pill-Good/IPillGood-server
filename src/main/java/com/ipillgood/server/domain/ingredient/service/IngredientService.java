package com.ipillgood.server.domain.ingredient.service;

import com.ipillgood.server.domain.ingredient.code.IngredientErrorCode;
import com.ipillgood.server.domain.ingredient.converter.IngredientConverter;
import com.ipillgood.server.domain.ingredient.dto.IngredientResponse;
import com.ipillgood.server.domain.ingredient.entity.Contraindication;
import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;
import com.ipillgood.server.domain.ingredient.exception.IngredientException;
import com.ipillgood.server.domain.ingredient.repository.ContraindicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientService {

    private static final List<ContraindicationType> SURVEY_CONTRAINDICATION_TYPES = List.of(
            ContraindicationType.MEDICATION,
            ContraindicationType.UNDERLYING_DISEASE,
            ContraindicationType.ALLERGY
    );

    private final ContraindicationRepository contraindicationRepository;

    public IngredientResponse.ContraindicationList getContraindications(String type, String keyword) {
        List<ContraindicationType> targetTypes = resolveTargetTypes(type);
        String normalizedKeyword = normalizeKeyword(keyword);

        List<Contraindication> contraindications = normalizedKeyword == null
                ? contraindicationRepository.findByTypeInOrderByIdAsc(targetTypes)
                : contraindicationRepository.findByTypeInAndConditionNameContainingOrderByIdAsc(
                        targetTypes,
                        normalizedKeyword
                );

        return IngredientConverter.toContraindicationList(contraindications, SURVEY_CONTRAINDICATION_TYPES);
    }

    private List<ContraindicationType> resolveTargetTypes(String type) {
        if (!StringUtils.hasText(type)) {
            return SURVEY_CONTRAINDICATION_TYPES;
        }

        ContraindicationType contraindicationType = parseType(type.trim());
        if (!SURVEY_CONTRAINDICATION_TYPES.contains(contraindicationType)) {
            throw new IngredientException(IngredientErrorCode.INVALID_CONTRAINDICATION_TYPE);
        }

        return List.of(contraindicationType);
    }

    private ContraindicationType parseType(String type) {
        try {
            return ContraindicationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IngredientException(IngredientErrorCode.INVALID_CONTRAINDICATION_TYPE);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return keyword.trim();
    }
}

