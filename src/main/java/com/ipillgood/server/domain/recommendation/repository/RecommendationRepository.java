package com.ipillgood.server.domain.recommendation.repository;

import com.ipillgood.server.domain.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}
