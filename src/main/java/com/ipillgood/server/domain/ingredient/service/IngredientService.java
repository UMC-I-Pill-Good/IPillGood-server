package com.ipillgood.server.domain.ingredient.service;

import com.ipillgood.server.domain.ingredient.converter.IngredientConverter;
import com.ipillgood.server.domain.ingredient.dto.IngredientResponse;
import com.ipillgood.server.domain.ingredient.entity.AlternativeFood;
import com.ipillgood.server.domain.ingredient.entity.Contraindication;
import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.ingredient.entity.IngredientCaution;
import com.ipillgood.server.domain.ingredient.entity.IngredientCombination;
import com.ipillgood.server.domain.ingredient.entity.IngredientEffect;
import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;
import com.ipillgood.server.domain.ingredient.exception.IngredientException;
import com.ipillgood.server.domain.ingredient.repository.AlternativeFoodRepository;
import com.ipillgood.server.domain.ingredient.repository.ContraindicationRepository;
import com.ipillgood.server.domain.ingredient.repository.IngredientCautionRepository;
import com.ipillgood.server.domain.ingredient.repository.IngredientCombinationRepository;
import com.ipillgood.server.domain.ingredient.repository.IngredientEffectRepository;
import com.ipillgood.server.domain.ingredient.repository.IngredientRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientService {

    private static final List<ContraindicationType> SURVEY_CONTRAINDICATION_TYPES = List.of(
            ContraindicationType.UNDERLYING_DISEASE,
            ContraindicationType.MEDICATION,
            ContraindicationType.ALLERGY
    );

    private final AlternativeFoodRepository alternativeFoodRepository;
    private final ContraindicationRepository contraindicationRepository;
    private final IngredientCautionRepository ingredientCautionRepository;
    private final IngredientCombinationRepository ingredientCombinationRepository;
    private final IngredientEffectRepository ingredientEffectRepository;
    private final IngredientRepository ingredientRepository;

    @Value("${app.storage.public-base-url:https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com}")
    private String storagePublicBaseUrl;

    public IngredientResponse.IngredientList getIngredients() {
        List<Ingredient> ingredients = ingredientRepository.findAllByOrderByIdAsc();
        return IngredientConverter.toIngredientList(ingredients, storagePublicBaseUrl);
    }

    public IngredientResponse.IngredientDetail getIngredient(Long ingredientId, Long memberId) {
        validateIngredientId(ingredientId);

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new IngredientException(GeneralErrorCode.NOT_FOUND));

        List<IngredientEffect> effects = ingredientEffectRepository.findByIngredientIdOrderByIdAsc(ingredientId);
        List<IngredientCaution> cautions = ingredientCautionRepository.findByIngredientIdOrderByIdAsc(ingredientId);
        List<IngredientCombination> combinations =
                ingredientCombinationRepository.findWithIngredientsByIngredientIdAndTypeOrderByIdAsc(
                        ingredientId,
                        CombinationType.CAUTION
                );
        List<AlternativeFood> alternativeFoods =
                alternativeFoodRepository.findByIngredientIdOrderByIdAsc(ingredientId);
        boolean hasCabinetProduct =
                ingredientRepository.countActiveCabinetProductsContainingIngredient(memberId, ingredientId) > 0;

        return IngredientConverter.toIngredientDetail(
                ingredient,
                effects,
                cautions,
                combinations,
                hasCabinetProduct,
                alternativeFoods,
                storagePublicBaseUrl
        );
    }

    public IngredientResponse.ContraindicationList getContraindications() {
        List<Contraindication> contraindications =
                contraindicationRepository.findByTypeInOrderByIdAsc(SURVEY_CONTRAINDICATION_TYPES);
        return IngredientConverter.toContraindicationList(contraindications, SURVEY_CONTRAINDICATION_TYPES);
    }

    private void validateIngredientId(Long ingredientId) {
        if (ingredientId == null || ingredientId < 1) {
            throw new IngredientException(GeneralErrorCode.BAD_REQUEST);
        }
    }
}
