package com.ipillgood.server.domain.recommendation.event;

import com.ipillgood.server.domain.recommendation.service.RecommendationGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 설문 저장 트랜잭션이 커밋된 이후에만 추천 생성을 트리거 (미커밋 데이터 조회 방지)
@Component
@RequiredArgsConstructor
public class RecommendationGenerationEventListener {

    private final RecommendationGenerationService recommendationGenerationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRecommendationGenerationRequested(RecommendationGenerationRequestedEvent event) {
        recommendationGenerationService.generate(event.recommendationId());
    }
}
