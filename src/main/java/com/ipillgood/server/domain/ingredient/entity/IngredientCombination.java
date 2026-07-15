package com.ipillgood.server.domain.ingredient.entity;

import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ingredient_combination")
public class IngredientCombination extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 성분 A
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_a_id", nullable = false)
    private Ingredient ingredientA;

    // 성분 B
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_b_id", nullable = false)
    private Ingredient ingredientB;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CombinationType type;

    // 조합 유형 BAD일 경우에만 이유 저장
    @Column(name = "reason", columnDefinition = "TEXT", nullable = true)
    private String reason;
}
