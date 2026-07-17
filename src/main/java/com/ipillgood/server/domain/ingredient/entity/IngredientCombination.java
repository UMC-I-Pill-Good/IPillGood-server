package com.ipillgood.server.domain.ingredient.entity;

import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 성분 궁합
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "ingredient_combination",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ingredient_combination",
                columnNames = {"ingredient_a_id", "ingredient_b_id", "type"}
        )
)
public class IngredientCombination extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_a_id", nullable = false)
    private Ingredient ingredientA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_b_id", nullable = false)
    private Ingredient ingredientB;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CombinationType type;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
}
