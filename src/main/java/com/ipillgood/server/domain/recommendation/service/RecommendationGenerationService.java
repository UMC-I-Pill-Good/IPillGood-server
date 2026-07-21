package com.ipillgood.server.domain.recommendation.service;

import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.ingredient.entity.IngredientEffect;
import com.ipillgood.server.domain.ingredient.repository.IngredientEffectRepository;
import com.ipillgood.server.domain.ingredient.repository.IngredientRepository;
import com.ipillgood.server.domain.recommendation.entity.Recommendation;
import com.ipillgood.server.domain.recommendation.entity.RecommendationItem;
import com.ipillgood.server.domain.recommendation.repository.RecommendationItemRepository;
import com.ipillgood.server.domain.recommendation.repository.RecommendationRepository;
import com.ipillgood.server.domain.survey.entity.SurveyResponse;
import com.ipillgood.server.domain.survey.entity.enums.OnboardingConcernCode;
import com.ipillgood.server.domain.survey.entity.enums.SurveySubmissionType;
import com.ipillgood.server.domain.survey.repository.SurveyContraindicationSelectionRepository;
import com.ipillgood.server.domain.survey.repository.SurveyCurrentIngredientSelectionRepository;
import com.ipillgood.server.domain.survey.repository.SurveyOnboardingConcernSelectionRepository;
import com.ipillgood.server.global.ai.GeminiClient;
import com.ipillgood.server.global.ai.dto.GeminiRecommendationResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 안전 후보 필터링 -> Gemini 호출 -> 2차 검증 -> 결과 저장까지의 추천 생성 전체 처리
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationGenerationService {

    private static final int MAX_RECOMMENDATION_ITEMS = 3;

    private final RecommendationRepository recommendationRepository;
    private final RecommendationItemRepository recommendationItemRepository;
    private final SurveyContraindicationSelectionRepository surveyContraindicationSelectionRepository;
    private final SurveyOnboardingConcernSelectionRepository surveyOnboardingConcernSelectionRepository;
    private final SurveyCurrentIngredientSelectionRepository surveyCurrentIngredientSelectionRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientEffectRepository ingredientEffectRepository;
    private final RecommendationPromptBuilder promptBuilder;
    private final GeminiClient geminiClient;

    @Async("recommendationTaskExecutor")
    @Transactional
    public void generate(Long recommendationId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId).orElse(null);
        if (recommendation == null) {
            log.warn("추천 생성 대상을 찾을 수 없습니다. recommendationId={}", recommendationId);
            return;
        }

        try {
            processGeneration(recommendation);
        } catch (Exception e) {
            log.error("추천 생성 실패. recommendationId={}", recommendationId, e);
            recommendation.markFailed(e.getMessage(), LocalDateTime.now());
        }
    }

    private void processGeneration(Recommendation recommendation) {
        SurveyResponse surveyResponse = recommendation.getSurveyResponse();

        List<Long> excludedContraindicationIds = surveyContraindicationSelectionRepository
                .findContraindicationIdsBySurveyResponseId(surveyResponse.getId());

        List<Ingredient> candidates = excludedContraindicationIds.isEmpty()
                ? ingredientRepository.findAll()
                : ingredientRepository.findSafeCandidates(excludedContraindicationIds);

        if (candidates.isEmpty()) {
            recommendation.markNoResult(null, LocalDateTime.now());
            return;
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

        GeminiRecommendationResult result = geminiClient.generateRecommendation(prompt);

        Map<Long, Ingredient> candidateById = candidates.stream()
                .collect(Collectors.toMap(Ingredient::getId, ingredient -> ingredient));

        // 2차 검증: 후보군에 실제로 속하는 ingredientId만 남긴다 (모델 환각 방지)
        List<GeminiRecommendationResult.Item> validItems = result.recommendations().stream()
                .filter(item -> candidateById.containsKey(item.ingredientId()))
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
                    .ingredient(candidateById.get(item.ingredientId()))
                    .rankNo(rankNo++)
                    .aiReason(item.aiReason())
                    .build());
        }

        LocalDateTime completedAt = LocalDateTime.now();
        recommendation.markSuccess(result.healthSummary(), completedAt);

        if (surveyResponse.getSubmissionType() == SurveySubmissionType.INITIAL) {
            recommendation.getMember().completeOnboarding(completedAt);
        }
    }
}
