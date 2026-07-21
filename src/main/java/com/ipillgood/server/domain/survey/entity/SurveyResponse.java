package com.ipillgood.server.domain.survey.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.survey.entity.enums.DietType;
import com.ipillgood.server.domain.survey.entity.enums.DrinkingStatus;
import com.ipillgood.server.domain.survey.entity.enums.ExerciseFrequency;
import com.ipillgood.server.domain.survey.entity.enums.JobType;
import com.ipillgood.server.domain.survey.entity.enums.SmokingStatus;
import com.ipillgood.server.domain.survey.entity.enums.SurveySubmissionType;
import com.ipillgood.server.global.entity.BaseEntity;
import com.ipillgood.server.global.enums.Gender;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 설문 기본 응답
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "survey_response")
public class SurveyResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_type", nullable = false)
    private SurveySubmissionType submissionType;

    @Column(name = "birth_year", nullable = false)
    private Short birthYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType;

    @Column(name = "menstrual_cycle_days")
    private Short menstrualCycleDays;

    @Column(name = "last_period_started_on")
    private LocalDate lastPeriodStartedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "smoking_status", nullable = false)
    private SmokingStatus smokingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "drinking_status", nullable = false)
    private DrinkingStatus drinkingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "diet_type", nullable = false)
    private DietType dietType;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_frequency", nullable = false)
    private ExerciseFrequency exerciseFrequency;

    @Column(name = "pregnant")
    private Boolean pregnant;

    @Column(name = "underlying_disease_none", nullable = false)
    private boolean underlyingDiseaseNone = false;

    @Column(name = "medication_none", nullable = false)
    private boolean medicationNone = false;

    @Column(name = "allergy_none", nullable = false)
    private boolean allergyNone = false;

    @Column(name = "current_ingredient_none", nullable = false)
    private boolean currentIngredientNone = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public SurveyResponse(Member member, SurveySubmissionType submissionType, Short birthYear, Gender gender,
                           JobType jobType, Short menstrualCycleDays, LocalDate lastPeriodStartedOn,
                           SmokingStatus smokingStatus, DrinkingStatus drinkingStatus, DietType dietType,
                           ExerciseFrequency exerciseFrequency, Boolean pregnant, boolean underlyingDiseaseNone,
                           boolean medicationNone, boolean allergyNone, boolean currentIngredientNone) {
        this.member = member;
        this.submissionType = submissionType;
        this.birthYear = birthYear;
        this.gender = gender;
        this.jobType = jobType;
        this.menstrualCycleDays = menstrualCycleDays;
        this.lastPeriodStartedOn = lastPeriodStartedOn;
        this.smokingStatus = smokingStatus;
        this.drinkingStatus = drinkingStatus;
        this.dietType = dietType;
        this.exerciseFrequency = exerciseFrequency;
        this.pregnant = pregnant;
        this.underlyingDiseaseNone = underlyingDiseaseNone;
        this.medicationNone = medicationNone;
        this.allergyNone = allergyNone;
        this.currentIngredientNone = currentIngredientNone;
    }

    // 설문 저장 완료 처리
    public void markCompleted(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
