package com.ipillgood.server.domain.survey.entity;

import com.ipillgood.server.domain.ingredient.entity.Contraindication;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// 설문 금기 조건 선택
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "survey_contraindication_selection",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_survey_contraindication_selection",
                columnNames = {"survey_response_id", "contraindication_id"}
        )
)
public class SurveyContraindicationSelection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SurveyResponse surveyResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contraindication_id", nullable = false)
    private Contraindication contraindication;

    @Builder
    public SurveyContraindicationSelection(SurveyResponse surveyResponse, Contraindication contraindication) {
        this.surveyResponse = surveyResponse;
        this.contraindication = contraindication;
    }
}
