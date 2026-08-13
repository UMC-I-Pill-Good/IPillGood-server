package com.ipillgood.server.domain.recommendation.service;

import com.ipillgood.server.domain.ingredient.entity.Contraindication;
import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.ingredient.entity.IngredientEffect;
import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;
import com.ipillgood.server.domain.ingredient.repository.ContraindicationRepository;
import com.ipillgood.server.domain.ingredient.repository.IngredientEffectRepository;
import com.ipillgood.server.domain.ingredient.repository.IngredientRepository;
import com.ipillgood.server.domain.recommendation.entity.Recommendation;
import com.ipillgood.server.domain.recommendation.entity.RecommendationFeedbackCycle;
import com.ipillgood.server.domain.recommendation.entity.RecommendationItem;
import com.ipillgood.server.domain.recommendation.repository.RecommendationFeedbackCycleRepository;
import com.ipillgood.server.domain.recommendation.repository.RecommendationItemRepository;
import com.ipillgood.server.domain.recommendation.repository.RecommendationRepository;
import com.ipillgood.server.domain.survey.entity.SurveyResponse;
import com.ipillgood.server.domain.survey.entity.enums.DrinkingStatus;
import com.ipillgood.server.domain.survey.entity.enums.OnboardingConcernCode;
import com.ipillgood.server.domain.survey.entity.enums.SmokingStatus;
import com.ipillgood.server.domain.survey.repository.SurveyContraindicationSelectionRepository;
import com.ipillgood.server.domain.survey.repository.SurveyCurrentIngredientSelectionRepository;
import com.ipillgood.server.domain.survey.repository.SurveyOnboardingConcernSelectionRepository;
import com.ipillgood.server.global.ai.dto.GeminiRecommendationResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 추천 생성 중 DB 트랜잭션이 필요한 단계(준비/결과 반영/실패 처리)만 담당한다.
// Gemini 호출 같은 외부 API 호출은 이 서비스 밖(RecommendationGenerationService)에서 트랜잭션 없이 수행한다.
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationGenerationTransactionService {

    private static final int MAX_RECOMMENDATION_ITEMS = 3;
    private static final int FEEDBACK_CYCLE_INTERVAL_DAYS = 30;

    private final RecommendationRepository recommendationRepository;
    private final RecommendationItemRepository recommendationItemRepository;
    private final RecommendationFeedbackCycleRepository recommendationFeedbackCycleRepository;
    private final SurveyContraindicationSelectionRepository surveyContraindicationSelectionRepository;
    private final SurveyOnboardingConcernSelectionRepository surveyOnboardingConcernSelectionRepository;
    private final SurveyCurrentIngredientSelectionRepository surveyCurrentIngredientSelectionRepository;
    private final ContraindicationRepository contraindicationRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientEffectRepository ingredientEffectRepository;
    private final RecommendationPromptBuilder promptBuilder;

    @Transactional
    public RecommendationGenerationContext prepareGeneration(Long recommendationId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId).orElse(null);
        if (recommendation == null) {
            log.warn("추천 생성 대상을 찾을 수 없습니다. recommendationId={}", recommendationId);
            return null;
        }

        SurveyResponse surveyResponse = recommendation.getSurveyResponse();

        List<Long> excludedContraindicationIds = new ArrayList<>(surveyContraindicationSelectionRepository
                .findContraindicationIdsBySurveyResponseId(surveyResponse.getId()));
        excludedContraindicationIds.addAll(resolveLifestyleContraindicationIds(surveyResponse));

        List<Ingredient> candidates = excludedContraindicationIds.isEmpty()
                ? ingredientRepository.findAll()
                : ingredientRepository.findSafeCandidates(excludedContraindicationIds);

        if (candidates.isEmpty()) {
            recommendation.markNoResult(null, LocalDateTime.now());
            return RecommendationGenerationContext.empty();
        }

        List<OnboardingConcernCode> concernCodes = surveyOnboardingConcernSelectionRepository
                .findConcernCodesBySurveyResponseId(surveyResponse.getId());

        List<Long> currentIngredientIds = surveyCurrentIngredientSelectionRepository
                .findIngredientIdsBySurveyResponseId(surveyResponse.getId());
        List<Ingredient> currentIngredients = currentIngredientIds.isEmpty()
                ? List.of()
                : ingredientRepository.findAllById(currentIngredientIds);

        Map<Long, List<String>> effectsByIngredientId = ingredientEffectRepository.findAllByIngredientIn(candidates)
                .stream()
                .collect(Collectors.groupingBy(effect -> effect.getIngredient().getId(),
                        Collectors.mapping(IngredientEffect::getEffect, Collectors.toList())));

        String prompt = promptBuilder.build(surveyResponse, concernCodes, currentIngredients, candidates,
                effectsByIngredientId);

        List<Long> candidateIngredientIds = candidates.stream().map(Ingredient::getId).toList();
        return new RecommendationGenerationContext(prompt, candidateIngredientIds);
    }

    // 임신/흡연/음주 여부에 해당하는 금기 조건을 후보 제외 목록에 포함시킨다
    private List<Long> resolveLifestyleContraindicationIds(SurveyResponse surveyResponse) {
        List<ContraindicationType> types = new ArrayList<>();
        if (Boolean.TRUE.equals(surveyResponse.getPregnant())) {
            types.add(ContraindicationType.PREGNANCY);
        }
        if (surveyResponse.getSmokingStatus() != SmokingStatus.NONE) {
            types.add(ContraindicationType.SMOKING);
        }
        if (surveyResponse.getDrinkingStatus() != DrinkingStatus.NONE) {
            types.add(ContraindicationType.DRINKING);
        }
        if (types.isEmpty()) {
            return List.of();
        }
        return contraindicationRepository.findByTypeInOrderByIdAsc(types).stream()
                .map(Contraindication::getId)
                .toList();
    }

    @Transactional
    public void applyGenerationResult(Long recommendationId, List<Long> candidateIngredientIds,
                                       GeminiRecommendationResult result) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId).orElse(null);
        if (recommendation == null) {
            log.warn("추천 생성 대상을 찾을 수 없습니다. recommendationId={}", recommendationId);
            return;
        }

        // 2차 검증: 후보군에 실제로 속하는 ingredientId만 남긴다 (모델 환각 방지)
        List<GeminiRecommendationResult.Item> validItems = result.recommendations().stream()
                .filter(item -> candidateIngredientIds.contains(item.ingredientId()))
                .toList();

        // AI가 추천 이유를 채우지 않은 항목이 하나라도 있으면 전체를 실패 처리한다
        for (GeminiRecommendationResult.Item item : validItems) {
            if (item.aiReason() == null || item.aiReason().isBlank()) {
                throw new IllegalStateException(
                        "AI가 추천 성분(ingredientId=" + item.ingredientId() + ")에 대한 추천 이유를 제공하지 않았습니다.");
            }
        }

        if (validItems.isEmpty()) {
            recommendation.markNoResult(result.healthSummary(), LocalDateTime.now());
            return;
        }

        List<GeminiRecommendationResult.Item> topItems = validItems.size() > MAX_RECOMMENDATION_ITEMS
                ? validItems.subList(0, MAX_RECOMMENDATION_ITEMS)
                : validItems;

        short rankNo = 1;
        for (GeminiRecommendationResult.Item item : topItems) {
            recommendationItemRepository.save(RecommendationItem.builder()
                    .recommendation(recommendation)
                    .ingredient(ingredientRepository.getReferenceById(item.ingredientId()))
                    .rankNo(rankNo++)
                    .aiReason(item.aiReason())
                    .build());
        }

        LocalDateTime completedAt = LocalDateTime.now();
        recommendation.markSuccess(result.healthSummary(), completedAt);

        recommendationFeedbackCycleRepository.save(RecommendationFeedbackCycle.builder()
                .member(recommendation.getMember())
                .recommendation(recommendation)
                .cycleDueOn(completedAt.toLocalDate().plusDays(FEEDBACK_CYCLE_INTERVAL_DAYS))
                .build());
    }

    @Transactional
    public void markFailed(Long recommendationId, String reason) {
        recommendationRepository.findById(recommendationId)
                .ifPresent(recommendation -> recommendation.markFailed(reason, LocalDateTime.now()));
    }
}
