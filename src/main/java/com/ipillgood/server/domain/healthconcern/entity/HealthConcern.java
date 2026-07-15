package com.ipillgood.server.domain.healthconcern.entity;

import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.healthconcern.entity.enums.MinorCategory;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "health_concern")
public class HealthConcern extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "major_category", nullable = false)
    private MajorCategory majorCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "minor_category", nullable = false)
    private MinorCategory minorCategory;

    @Column(name = "decline_cause", nullable = false, columnDefinition = "TEXT")
    private String declineCause;

}
