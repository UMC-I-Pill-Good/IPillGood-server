package com.ipillgood.server.domain.ingredient.converter;

import com.ipillgood.server.domain.ingredient.dto.IngredientResponse;
import com.ipillgood.server.domain.ingredient.entity.AlternativeFood;
import com.ipillgood.server.domain.ingredient.entity.Contraindication;
import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.ingredient.entity.IngredientCaution;
import com.ipillgood.server.domain.ingredient.entity.IngredientCombination;
import com.ipillgood.server.domain.ingredient.entity.IngredientEffect;
import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;

import java.util.List;

public class IngredientConverter {

    private IngredientConverter() {
    }

    public static IngredientResponse.IngredientList toIngredientList(
            List<Ingredient> ingredients,
            String imageBaseUrl
    ) {
        return IngredientResponse.IngredientList.builder()
                .ingredients(ingredients.stream()
                        .map(ingredient -> toIngredientSummary(ingredient, imageBaseUrl))
                        .toList())
                .build();
    }

    private static IngredientResponse.IngredientSummary toIngredientSummary(
            Ingredient ingredient,
            String imageBaseUrl
    ) {
        return IngredientResponse.IngredientSummary.builder()
                .ingredientId(ingredient.getId())
                .name(ingredient.getName())
                .imageUrl(toImageUrl(imageBaseUrl, ingredient.getImageKey()))
                .build();
    }

    public static IngredientResponse.IngredientDetail toIngredientDetail(
            Ingredient ingredient,
            List<IngredientEffect> effects,
            List<IngredientCaution> cautions,
            List<IngredientCombination> combinations,
            boolean hasCabinetProduct,
            List<AlternativeFood> alternativeFoods,
            String imageBaseUrl
    ) {
        return IngredientResponse.IngredientDetail.builder()
                .ingredientId(ingredient.getId())
                .name(ingredient.getName())
                .description(ingredient.getDescription())
                .imageUrl(toImageUrl(imageBaseUrl, ingredient.getImageKey()))
                .effects(effects.stream()
                        .map(IngredientEffect::getEffect)
                        .toList())
                .cautions(cautions.stream()
                        .map(IngredientCaution::getCaution)
                        .toList())
                .contraindicatedCombinations(combinations.stream()
                        .map(combination -> toContraindicatedCombination(ingredient.getId(), combination))
                        .toList())
                .recommendedIntake(ingredient.getRecommendedIntake())
                .recommendedIntakeTime(ingredient.getRecommendedIntakeTime())
                .hasCabinetProduct(hasCabinetProduct)
                .alternativeFoods(alternativeFoods.stream()
                        .map(IngredientConverter::toAlternativeFoodItem)
                        .toList())
                .build();
    }

    private static IngredientResponse.ContraindicatedCombination toContraindicatedCombination(
            Long currentIngredientId,
            IngredientCombination combination
    ) {
        Ingredient targetIngredient = combination.getIngredientA().getId().equals(currentIngredientId)
                ? combination.getIngredientB()
                : combination.getIngredientA();

        return IngredientResponse.ContraindicatedCombination.builder()
                .targetIngredientId(targetIngredient.getId())
                .targetIngredientName(targetIngredient.getName())
                .type(combination.getType())
                .reason(combination.getReason())
                .build();
    }

    private static IngredientResponse.AlternativeFoodItem toAlternativeFoodItem(AlternativeFood alternativeFood) {
        return IngredientResponse.AlternativeFoodItem.builder()
                .name(alternativeFood.getName())
                .contentPer100g(alternativeFood.getContentPer100g())
                .build();
    }

    private static String toImageUrl(String imageBaseUrl, String imageKey) {
        String normalizedBaseUrl = imageBaseUrl.endsWith("/")
                ? imageBaseUrl.substring(0, imageBaseUrl.length() - 1)
                : imageBaseUrl;
        String normalizedImageKey = imageKey.startsWith("/")
                ? imageKey.substring(1)
                : imageKey;
        return normalizedBaseUrl + "/" + normalizedImageKey;
    }

    public static IngredientResponse.ContraindicationList toContraindicationList(
            List<Contraindication> contraindications,
            List<ContraindicationType> surveyTypes
    ) {
        return IngredientResponse.ContraindicationList.builder()
                .groups(surveyTypes.stream()
                        .map(type -> toContraindicationGroup(type, contraindications))
                        .toList())
                .build();
    }

    private static IngredientResponse.ContraindicationGroup toContraindicationGroup(
            ContraindicationType type,
            List<Contraindication> contraindications
    ) {
        return IngredientResponse.ContraindicationGroup.builder()
                .type(type)
                .label(toSurveyLabel(type))
                .items(contraindications.stream()
                        .filter(contraindication -> contraindication.getType() == type)
                        .map(IngredientConverter::toContraindicationItem)
                        .toList())
                .build();
    }

    private static String toSurveyLabel(ContraindicationType type) {
        return switch (type) {
            case UNDERLYING_DISEASE -> "기저질환";
            case MEDICATION -> "현재 복용 중인 약";
            case ALLERGY -> "알러지";
            default -> type.getLabel();
        };
    }

    private static IngredientResponse.ContraindicationItem toContraindicationItem(Contraindication contraindication) {
        return IngredientResponse.ContraindicationItem.builder()
                .contraindicationId(contraindication.getId())
                .conditionName(contraindication.getConditionName())
                .build();
    }
}
