package com.ipillgood.server.domain.ingredient.entity;

import com.ipillgood.server.domain.ingredient.entity.enums.TargetAgeGroup;
import com.ipillgood.server.domain.ingredient.entity.enums.TargetGender;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ingredient")
public class Ingredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "recommended_intake", nullable = false)
    private String recommendedIntake;

    @Column(name = "recommended_intake_time", nullable = false)
    private String recommendedIntakeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_gender", nullable = false)
    private TargetGender recommendedGender;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_age_group", nullable = false)
    private TargetAgeGroup recommendedAgeGroup;

    @Column(name = "image_key", nullable = false)
    private String imageKey;
}
