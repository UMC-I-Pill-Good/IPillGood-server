package com.ipillgood.server.domain.survey.entity;

import com.ipillgood.server.domain.survey.entity.enums.OnboardingConcernCode;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

// 온보딩 건강 고민 선택
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "survey_onboarding_concern_selection",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_survey_onboarding_concern_selection",
                columnNames = {"survey_response_id", "concern_code"}
        )
)
public class SurveyOnboardingConcernSelection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SurveyResponse surveyResponse;

    @Enumerated(EnumType.STRING)
    @Column(name = "concern_code", nullable = false)
    private OnboardingConcernCode concernCode;

    @Column(name = "priority")
    private Short priority;

    @Builder
    public SurveyOnboardingConcernSelection(SurveyResponse surveyResponse, OnboardingConcernCode concernCode) {
        this.surveyResponse = surveyResponse;
        this.concernCode = concernCode;
    }
}
