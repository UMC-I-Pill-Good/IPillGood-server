package com.ipillgood.server.domain.ingredient.entity;

import com.ipillgood.server.domain.ingredient.entity.enums.TargetAgeGroup;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "ingredient_age_group",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ingredient_age_group",
                columnNames = {"ingredient_id", "age_group"}
        )
)
public class IngredientAgeGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false)
    private TargetAgeGroup ageGroup;
}
