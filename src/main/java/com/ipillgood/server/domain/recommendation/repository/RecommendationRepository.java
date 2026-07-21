package com.ipillgood.server.domain.recommendation.repository;

import com.ipillgood.server.domain.recommendation.entity.Recommendation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    // 홈에 노출할 "현재 활성" 추천 = 가장 최근에 활성화(SUCCESS 전환)된 추천
    Optional<Recommendation> findFirstByMemberIdAndActivatedAtIsNotNullOrderByActivatedAtDesc(Long memberId);
}
