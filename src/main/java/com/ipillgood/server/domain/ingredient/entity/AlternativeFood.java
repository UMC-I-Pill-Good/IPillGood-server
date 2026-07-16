package com.ipillgood.server.domain.ingredient.entity;

import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// alternative_food 테이블 매핑
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "alternative_food")
public class AlternativeFood extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "content_per_100g", nullable = false, length = 100)
    private String contentPer100g;
}
