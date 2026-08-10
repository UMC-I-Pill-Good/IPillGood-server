package com.ipillgood.server.domain.recommendation.service;

import com.ipillgood.server.domain.recommendation.code.RecommendationErrorCode;
import com.ipillgood.server.domain.recommendation.converter.RecommendationFeedbackCycleConverter;
import com.ipillgood.server.domain.recommendation.dto.RecommendationFeedbackCycleRequest;
import com.ipillgood.server.domain.recommendation.dto.RecommendationFeedbackCycleResponse;
import com.ipillgood.server.domain.recommendation.entity.RecommendationFeedbackCycle;
import com.ipillgood.server.domain.recommendation.exception.RecommendationException;
import com.ipillgood.server.domain.recommendation.repository.RecommendationFeedbackCycleRepository;
import com.ipillgood.server.domain.recommendation.repository.RecommendationRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationFeedbackCycleService {

    private static final int NEXT_CYCLE_INTERVAL_DAYS = 30;

    private final RecommendationFeedbackCycleRepository recommendationFeedbackCycleRepository;
    private final RecommendationRepository recommendationRepository;

    public RecommendationFeedbackCycleResponse.Due getDue(Long memberId) {
        return recommendationRepository
                .findFirstByMemberIdAndActivatedAtIsNotNullOrderByActivatedAtDesc(memberId)
                .flatMap(recommendation -> recommendationFeedbackCycleRepository
                        .findFirstByRecommendation_IdAndRespondedAtIsNullAndCycleDueOnLessThanEqualOrderByCycleDueOnAsc(
                                recommendation.getId(), LocalDate.now()))
                .map(RecommendationFeedbackCycleConverter::toDue)
                .orElseGet(RecommendationFeedbackCycleConverter::toNotDue);
    }

    @Transactional
    public RecommendationFeedbackCycleResponse.Respond respond(
            Long memberId, Long cycleId, RecommendationFeedbackCycleRequest.Respond request) {
        if (request == null || request.responseType() == null) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }

        RecommendationFeedbackCycle cycle = recommendationFeedbackCycleRepository.findById(cycleId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        if (!cycle.getMember().getId().equals(memberId)) {
            throw new GeneralException(GeneralErrorCode.NOT_FOUND);
        }

        if (cycle.getRespondedAt() != null) {
            throw new RecommendationException(RecommendationErrorCode.FEEDBACK_CYCLE_ALREADY_RESPONDED);
        }

        if (cycle.getCycleDueOn().isAfter(LocalDate.now())) {
            throw new RecommendationException(RecommendationErrorCode.FEEDBACK_CYCLE_NOT_DUE);
        }

        LocalDateTime respondedAt = LocalDateTime.now();
        LocalDate nextCycleDueOn = respondedAt.toLocalDate().plusDays(NEXT_CYCLE_INTERVAL_DAYS);
        cycle.respond(request.responseType(), respondedAt, nextCycleDueOn);

        // 응답을 받은 시점에 다음 30일 주기 사이클을 미리 만들어 반복 체크가 이어지도록 한다
        recommendationFeedbackCycleRepository.save(RecommendationFeedbackCycle.builder()
                .member(cycle.getMember())
                .recommendation(cycle.getRecommendation())
                .cycleDueOn(nextCycleDueOn)
                .build());

        return RecommendationFeedbackCycleConverter.toRespond(cycle);
    }
}
