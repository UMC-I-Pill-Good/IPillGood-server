package com.ipillgood.server.domain.recommendation.repository;

import com.ipillgood.server.domain.recommendation.entity.RecommendationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationItemRepository extends JpaRepository<RecommendationItem, Long> {
}
