package com.ipillgood.server.domain.ingredient.converter;

import com.ipillgood.server.domain.ingredient.dto.IngredientResponse;
import com.ipillgood.server.domain.ingredient.entity.Contraindication;
import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IngredientConverter {

    public static IngredientResponse.ContraindicationList toContraindicationList(
            List<Contraindication> contraindications,
            List<ContraindicationType> surveyTypes
    ) {
        return IngredientResponse.ContraindicationList.builder()
                .contraindications(toContraindicationItems(contraindications))
                .groupedByType(toGroupedByType(contraindications, surveyTypes))
                .build();
    }

    private static List<IngredientResponse.ContraindicationItem> toContraindicationItems(
            List<Contraindication> contraindications
    ) {
        return contraindications.stream()
                .map(IngredientConverter::toContraindicationItem)
                .toList();
    }

    private static IngredientResponse.ContraindicationItem toContraindicationItem(Contraindication contraindication) {
        return IngredientResponse.ContraindicationItem.builder()
                .contraindicationId(contraindication.getId())
                .type(contraindication.getType())
                .conditionName(contraindication.getConditionName())
                .build();
    }

    private static Map<String, List<IngredientResponse.GroupedContraindicationItem>> toGroupedByType(
            List<Contraindication> contraindications,
            List<ContraindicationType> surveyTypes
    ) {
        Map<String, List<IngredientResponse.GroupedContraindicationItem>> groupedByType = new LinkedHashMap<>();

        for (ContraindicationType type : surveyTypes) {
            List<IngredientResponse.GroupedContraindicationItem> items = contraindications.stream()
                    .filter(contraindication -> contraindication.getType() == type)
                    .map(IngredientConverter::toGroupedContraindicationItem)
                    .toList();
            groupedByType.put(type.name(), items);
        }

        return groupedByType;
    }

    private static IngredientResponse.GroupedContraindicationItem toGroupedContraindicationItem(
            Contraindication contraindication
    ) {
        return IngredientResponse.GroupedContraindicationItem.builder()
                .contraindicationId(contraindication.getId())
                .conditionName(contraindication.getConditionName())
                .build();
    }
}

