package com.ipillgood.server.domain.ingredient.entity;

import com.ipillgood.server.domain.ingredient.entity.enums.TargetGender;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 영양성분 마스터
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ingredient")
public class Ingredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "recommended_intake", length = 100)
    private String recommendedIntake;

    @Column(name = "recommended_intake_time")
    private String recommendedIntakeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_gender", nullable = false)
    private TargetGender recommendedGender;

    @Column(name = "image_key", nullable = false)
    private String imageKey;

    @Column(name = "ad_claim_risk", nullable = false)
    private boolean adClaimRisk = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient ingredient = (Ingredient) o;
        return id.equals(ingredient.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
