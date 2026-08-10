package com.ipillgood.server.domain.recommendation.service;

import com.ipillgood.server.domain.ingredient.entity.EffectKeyword;
import com.ipillgood.server.domain.ingredient.repository.EffectKeywordRepository;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.recommendation.code.RecommendationErrorCode;
import com.ipillgood.server.domain.recommendation.converter.RecommendationConverter;
import com.ipillgood.server.domain.recommendation.dto.RecommendationResponse;
import com.ipillgood.server.domain.recommendation.entity.Recommendation;
import com.ipillgood.server.domain.recommendation.entity.RecommendationItem;
import com.ipillgood.server.domain.recommendation.entity.enums.RecommendationStatus;
import com.ipillgood.server.domain.recommendation.event.RecommendationGenerationRequestedEvent;
import com.ipillgood.server.domain.recommendation.exception.RecommendationException;
import com.ipillgood.server.domain.recommendation.repository.RecommendationItemRepository;
import com.ipillgood.server.domain.recommendation.repository.RecommendationRepository;
import com.ipillgood.server.domain.survey.entity.enums.SurveySubmissionType;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import com.ipillgood.server.global.s3.S3Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final Set<RecommendationStatus> RETRYABLE_STATUSES =
            Set.of(RecommendationStatus.FAILED, RecommendationStatus.NO_RESULT);

    private final RecommendationRepository recommendationRepository;
    private final RecommendationItemRepository recommendationItemRepository;
    private final EffectKeywordRepository effectKeywordRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final S3Service s3Service;

    public RecommendationResponse.Detail getCurrentRecommendation(Long memberId) {
        Recommendation recommendation = recommendationRepository
                .findFirstByMemberIdAndActivatedAtIsNotNullOrderByActivatedAtDesc(memberId)
                .orElseThrow(() -> new RecommendationException(RecommendationErrorCode.CURRENT_RECOMMENDATION_NOT_FOUND));

        return toDetail(recommendation);
    }

    public RecommendationResponse.Detail getRecommendation(Long memberId, Long recommendationId) {
        Recommendation recommendation = getOwnedRecommendation(memberId, recommendationId);
        return toDetail(recommendation);
    }

    @Transactional
    public RecommendationResponse.Retry retry(Long memberId, Long recommendationId) {
        Recommendation recommendation = getOwnedRecommendation(memberId, recommendationId);
        if (!RETRYABLE_STATUSES.contains(recommendation.getStatus())) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RETRY_STATUS);
        }

        recommendation.markRetried(LocalDateTime.now());
        eventPublisher.publishEvent(new RecommendationGenerationRequestedEvent(recommendation.getId()));

        return RecommendationConverter.toRetry(recommendation);
    }

    @Transactional
    public RecommendationResponse.Confirm confirm(Long memberId, Long recommendationId) {
        Recommendation recommendation = getOwnedRecommendation(memberId, recommendationId);
        if (recommendation.getStatus() != RecommendationStatus.SUCCESS) {
            throw new RecommendationException(RecommendationErrorCode.NOT_CONFIRMABLE_STATUS);
        }

        Member member = recommendation.getMember();
        if (recommendation.getSurveyResponse().getSubmissionType() == SurveySubmissionType.INITIAL
                && member.getOnboardingCompletedAt() == null) {
            member.completeOnboarding(LocalDateTime.now());
        }

        return RecommendationConverter.toConfirm(recommendation);
    }

    private Recommendation getOwnedRecommendation(Long memberId, Long recommendationId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        if (!recommendation.getMember().getId().equals(memberId)) {
            throw new GeneralException(GeneralErrorCode.NOT_FOUND);
        }

        return recommendation;
    }

    private RecommendationResponse.Detail toDetail(Recommendation recommendation) {
        List<RecommendationItem> items = recommendationItemRepository
                .findByRecommendationIdOrderByRankNoAsc(recommendation.getId());

        Map<Long, List<String>> effectKeywordsByIngredientId = toEffectKeywordsByIngredientId(items);

        return RecommendationConverter.toDetail(recommendation, items, effectKeywordsByIngredientId, s3Service::getPublicUrl);
    }

    private Map<Long, List<String>> toEffectKeywordsByIngredientId(List<RecommendationItem> items) {
        List<Long> ingredientIds = items.stream()
                .map(item -> item.getIngredient().getId())
                .toList();

        if (ingredientIds.isEmpty()) {
            return Map.of();
        }

        return effectKeywordRepository.findByIngredient_IdInOrderByIdAsc(ingredientIds).stream()
                .collect(Collectors.groupingBy(
                        keyword -> keyword.getIngredient().getId(),
                        Collectors.mapping(EffectKeyword::getKeyword, Collectors.toList())));
    }
}
