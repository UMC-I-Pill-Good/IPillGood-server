package com.ipillgood.server.domain.survey.service;

import com.ipillgood.server.domain.ingredient.entity.Contraindication;
import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;
import com.ipillgood.server.domain.ingredient.repository.ContraindicationRepository;
import com.ipillgood.server.domain.ingredient.repository.IngredientRepository;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.recommendation.entity.Recommendation;
import com.ipillgood.server.domain.recommendation.entity.enums.RecommendationStatus;
import com.ipillgood.server.domain.recommendation.event.RecommendationGenerationRequestedEvent;
import com.ipillgood.server.domain.recommendation.repository.RecommendationRepository;
import com.ipillgood.server.domain.survey.converter.SurveyConverter;
import com.ipillgood.server.domain.survey.dto.SurveyRequest;
import com.ipillgood.server.domain.survey.dto.SurveyResult;
import com.ipillgood.server.domain.survey.code.SurveyErrorCode;
import com.ipillgood.server.domain.survey.entity.SurveyResponse;
import com.ipillgood.server.domain.survey.exception.SurveyException;
import com.ipillgood.server.domain.survey.repository.SurveyContraindicationSelectionRepository;
import com.ipillgood.server.domain.survey.repository.SurveyCurrentIngredientSelectionRepository;
import com.ipillgood.server.domain.survey.repository.SurveyOnboardingConcernSelectionRepository;
import com.ipillgood.server.domain.survey.repository.SurveyProjection;
import com.ipillgood.server.domain.survey.repository.SurveyResponseRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import com.ipillgood.server.global.enums.Gender;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    // 설문에서 목록 선택으로 받는 금기 조건 유형 (음주/임신/흡연은 전용 필드로 별도 입력)
    private static final List<ContraindicationType> SELECTABLE_CONTRAINDICATION_TYPES = List.of(
            ContraindicationType.UNDERLYING_DISEASE,
            ContraindicationType.MEDICATION,
            ContraindicationType.ALLERGY
    );

    private static final int MIN_CONCERN_CODE_COUNT = 1;
    private static final int MAX_CONCERN_CODE_COUNT = 3;

    private final MemberRepository memberRepository;
    private final ContraindicationRepository contraindicationRepository;
    private final IngredientRepository ingredientRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyContraindicationSelectionRepository surveyContraindicationSelectionRepository;
    private final SurveyOnboardingConcernSelectionRepository surveyOnboardingConcernSelectionRepository;
    private final SurveyCurrentIngredientSelectionRepository surveyCurrentIngredientSelectionRepository;
    private final RecommendationRepository recommendationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SurveyResult.Submit submit(Long memberId, SurveyRequest.Submit request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        validateBirthYear(request.birthYear());
        validatePregnant(request.gender(), request.pregnant());
        validateMenstrualInfo(request.gender(), request.menstrualCycleDays(), request.lastPeriodStartedOn());
        validateConcernCodeCount(request.onboardingConcernCodes());
        validateCurrentIngredientSelection(request.currentIngredientNone(), request.currentIngredientIds());

        List<Contraindication> contraindications = resolveContraindications(request.contraindicationIds());
        validateContraindicationSelections(request, contraindications);

        List<Ingredient> currentIngredients = resolveIngredients(request.currentIngredientIds());

        SurveyResponse surveyResponse = SurveyConverter.toSurveyResponse(request, member);
        surveyResponse = surveyResponseRepository.save(surveyResponse);

        surveyContraindicationSelectionRepository.saveAll(
                SurveyConverter.toContraindicationSelections(surveyResponse, contraindications));

        surveyOnboardingConcernSelectionRepository.saveAll(
                SurveyConverter.toConcernSelections(surveyResponse, request.onboardingConcernCodes()));

        surveyCurrentIngredientSelectionRepository.saveAll(
                SurveyConverter.toCurrentIngredientSelections(surveyResponse, currentIngredients));

        surveyResponse.markCompleted(LocalDateTime.now());

        Recommendation recommendation = Recommendation.builder()
                .member(member)
                .surveyResponse(surveyResponse)
                .status(RecommendationStatus.PENDING)
                .startedAt(LocalDateTime.now())
                .build();
        recommendation = recommendationRepository.save(recommendation);

        eventPublisher.publishEvent(new RecommendationGenerationRequestedEvent(recommendation.getId()));

        return SurveyConverter.toSubmitResult(surveyResponse, recommendation);
    }

    public Map<Long, SurveyProjection.MemberProfile> getLatestProfiles(Collection<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, SurveyResponse> latestByMemberId = surveyResponseRepository
                .findLatestCompletedByMemberIds(memberIds).stream()
                .collect(Collectors.toMap(
                        response -> response.getMember().getId(),
                        Function.identity()
                        )
                );

        return latestByMemberId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new SurveyProjection.MemberProfile(
                                entry.getKey(),
                                entry.getValue().getBirthYear(),
                                entry.getValue().getGender())));
    }

    private void validateBirthYear(Integer birthYear) {
        int currentYear = Year.now().getValue();
        if (birthYear > currentYear) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
    }

    private void validatePregnant(Gender gender, Boolean pregnant) {
        if (gender == Gender.FEMALE && pregnant == null) {
            throw new SurveyException(SurveyErrorCode.PREGNANT_REQUIRED_FOR_FEMALE);
        }
        if (gender == Gender.MALE && pregnant != null) {
            throw new SurveyException(SurveyErrorCode.PREGNANT_NOT_ALLOWED_FOR_MALE);
        }
    }

    private void validateMenstrualInfo(Gender gender, Integer menstrualCycleDays,
                                        java.time.LocalDate lastPeriodStartedOn) {
        boolean hasCycle = menstrualCycleDays != null;
        boolean hasLastPeriod = lastPeriodStartedOn != null;
        if (hasCycle != hasLastPeriod) {
            throw new SurveyException(SurveyErrorCode.MENSTRUAL_INFO_INCOMPLETE);
        }
        if (gender == Gender.MALE && (hasCycle || hasLastPeriod)) {
            throw new SurveyException(SurveyErrorCode.MENSTRUAL_INFO_NOT_ALLOWED_FOR_MALE);
        }
    }

    private void validateConcernCodeCount(List<?> onboardingConcernCodes) {
        int count = onboardingConcernCodes.size();
        if (count < MIN_CONCERN_CODE_COUNT || count > MAX_CONCERN_CODE_COUNT) {
            throw new SurveyException(SurveyErrorCode.CONCERN_CODE_COUNT_INVALID);
        }
    }

    private void validateCurrentIngredientSelection(boolean currentIngredientNone, List<Long> currentIngredientIds) {
        if (currentIngredientNone && !currentIngredientIds.isEmpty()) {
            throw new SurveyException(SurveyErrorCode.NONE_AND_DETAIL_CONFLICT);
        }
        if (!currentIngredientNone && currentIngredientIds.isEmpty()) {
            throw new SurveyException(SurveyErrorCode.CURRENT_INGREDIENT_DETAIL_REQUIRED);
        }
    }

    private List<Contraindication> resolveContraindications(List<Long> contraindicationIds) {
        if (contraindicationIds.isEmpty()) {
            return List.of();
        }
        List<Contraindication> contraindications = contraindicationRepository.findAllById(contraindicationIds);
        if (contraindications.size() != new HashSet<>(contraindicationIds).size()) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
        boolean hasDisallowedType = contraindications.stream()
                .anyMatch(c -> !SELECTABLE_CONTRAINDICATION_TYPES.contains(c.getType()));
        if (hasDisallowedType) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
        return contraindications;
    }

    private void validateContraindicationSelections(SurveyRequest.Submit request, List<Contraindication> contraindications) {
        Map<ContraindicationType, Set<Long>> idsByType = contraindications.stream()
                .collect(Collectors.groupingBy(Contraindication::getType,
                        Collectors.mapping(Contraindication::getId, Collectors.toSet())));

        validateContraindicationType(request.underlyingDiseaseNone(), idsByType, ContraindicationType.UNDERLYING_DISEASE);
        validateContraindicationType(request.medicationNone(), idsByType, ContraindicationType.MEDICATION);
        validateContraindicationType(request.allergyNone(), idsByType, ContraindicationType.ALLERGY);
    }

    private void validateContraindicationType(boolean noneFlag, Map<ContraindicationType, Set<Long>> idsByType,
                                               ContraindicationType type) {
        Set<Long> idsOfType = idsByType.getOrDefault(type, Set.of());
        if (noneFlag && !idsOfType.isEmpty()) {
            throw new SurveyException(SurveyErrorCode.NONE_AND_DETAIL_CONFLICT);
        }
        if (!noneFlag && idsOfType.isEmpty()) {
            throw new SurveyException(SurveyErrorCode.CONTRAINDICATION_DETAIL_REQUIRED);
        }
    }

    private List<Ingredient> resolveIngredients(List<Long> ingredientIds) {
        if (ingredientIds.isEmpty()) {
            return List.of();
        }
        List<Ingredient> ingredients = ingredientRepository.findAllById(ingredientIds);
        if (ingredients.size() != new HashSet<>(ingredientIds).size()) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
        return ingredients;
    }
}
