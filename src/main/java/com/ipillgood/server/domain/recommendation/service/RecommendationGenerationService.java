package com.ipillgood.server.domain.recommendation.service;

import com.ipillgood.server.global.ai.GeminiClient;
import com.ipillgood.server.global.ai.dto.GeminiRecommendationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// 안전 후보 필터링 -> Gemini 호출 -> 2차 검증 -> 결과 저장까지의 추천 생성 전체 처리를 조율한다.
// Gemini 호출(외부 API, 블로킹 I/O)이 DB 트랜잭션에 물리지 않도록 준비/외부호출/반영 단계를
// RecommendationGenerationTransactionService로 분리하고, 이 클래스 자체는 트랜잭션을 갖지 않는다.
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationGenerationService {

    private final RecommendationGenerationTransactionService transactionService;
    private final GeminiClient geminiClient;

    @Async("recommendationTaskExecutor")
    public void generate(Long recommendationId) {
        try {
            RecommendationGenerationContext context = transactionService.prepareGeneration(recommendationId);
            if (context == null || context.candidateIngredientIds().isEmpty()) {
                return;
            }

            GeminiRecommendationResult result = geminiClient.generateRecommendation(context.prompt());
            transactionService.applyGenerationResult(recommendationId, context.candidateIngredientIds(), result);
        } catch (Exception e) {
            log.error("추천 생성 실패. recommendationId={}", recommendationId, e);
            transactionService.markFailed(recommendationId, e.getMessage());
        }
    }
}
