package com.ipillgood.server.domain.ingredient.entity;

import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// effect_keyword 테이블 매핑
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "effect_keyword",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_effect_keyword",
                columnNames = {"ingredient_id", "keyword"}
        )
)
public class EffectKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "keyword", nullable = false, length = 50)
    private String keyword;
}
