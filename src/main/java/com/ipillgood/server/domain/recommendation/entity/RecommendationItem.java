package com.ipillgood.server.domain.recommendation.entity;

import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// recommendation_item 테이블 매핑
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "recommendation_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recommendation_item_rank",
                        columnNames = {"recommendation_id", "rank_no"}
                ),
                @UniqueConstraint(
                        name = "uk_recommendation_item_ingredient",
                        columnNames = {"recommendation_id", "ingredient_id"}
                )
        }
)
public class RecommendationItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "rank_no", nullable = false)
    private Short rankNo;

    @Column(name = "ai_reason", nullable = false, columnDefinition = "TEXT")
    private String aiReason;
}
