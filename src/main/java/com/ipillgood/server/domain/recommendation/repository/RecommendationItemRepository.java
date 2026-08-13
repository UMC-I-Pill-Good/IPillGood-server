package com.ipillgood.server.domain.recommendation.repository;

import com.ipillgood.server.domain.recommendation.entity.RecommendationItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendationItemRepository extends JpaRepository<RecommendationItem, Long> {

    @Query("""
            select ri from RecommendationItem ri
            join fetch ri.ingredient
            where ri.recommendation.id = :recommendationId
            order by ri.rankNo asc
            """)
    List<RecommendationItem> findByRecommendationIdOrderByRankNoAsc(@Param("recommendationId") Long recommendationId);
}
