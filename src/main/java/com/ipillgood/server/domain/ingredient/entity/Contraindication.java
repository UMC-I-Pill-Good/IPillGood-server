package com.ipillgood.server.domain.ingredient.entity;

import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 금기 조건 마스터
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "contraindication",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_contraindication",
                columnNames = {"type", "condition_name"}
        )
)
public class Contraindication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ContraindicationType type;

    @Column(name = "condition_name", nullable = false, length = 100)
    private String conditionName;
}
