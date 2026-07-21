package com.ipillgood.server.domain.recommendation.repository;

import com.ipillgood.server.domain.recommendation.entity.RecommendationFeedbackCycle;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationFeedbackCycleRepository extends JpaRepository<RecommendationFeedbackCycle, Long> {

    // 아직 응답하지 않았고 예정일이 도래한 피드백 사이클 중 가장 먼저 도래한 것
    Optional<RecommendationFeedbackCycle> findFirstByMember_IdAndRespondedAtIsNullAndCycleDueOnLessThanEqualOrderByCycleDueOnAsc(
            Long memberId, LocalDate today);
}
