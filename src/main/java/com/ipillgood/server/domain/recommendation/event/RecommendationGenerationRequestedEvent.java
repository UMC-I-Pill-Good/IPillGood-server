package com.ipillgood.server.domain.recommendation.event;

// 설문 저장 트랜잭션 커밋 후 추천 생성을 비동기로 트리거하기 위한 이벤트
public record RecommendationGenerationRequestedEvent(Long recommendationId) {
}
