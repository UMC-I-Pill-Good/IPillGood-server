package com.ipillgood.server.domain.ingredient.entity;

import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 금기 조건별 주의 성분
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "contraindication_ingredient",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_contraindication_ingredient",
                columnNames = {"contraindication_id", "ingredient_id"}
        )
)
public class ContraindicationIngredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contraindication_id", nullable = false)
    private Contraindication contraindication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

}
