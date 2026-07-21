package com.ipillgood.server.domain.survey.converter;

import com.ipillgood.server.domain.ingredient.entity.Contraindication;
import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.recommendation.entity.Recommendation;
import com.ipillgood.server.domain.survey.dto.SurveyRequest;
import com.ipillgood.server.domain.survey.dto.SurveyResult;
import com.ipillgood.server.domain.survey.entity.SurveyContraindicationSelection;
import com.ipillgood.server.domain.survey.entity.SurveyCurrentIngredientSelection;
import com.ipillgood.server.domain.survey.entity.SurveyOnboardingConcernSelection;
import com.ipillgood.server.domain.survey.entity.SurveyResponse;
import com.ipillgood.server.domain.survey.entity.enums.OnboardingConcernCode;
import java.util.List;

public class SurveyConverter {

    // 설문 요청 -> SurveyResponse 엔티티
    public static SurveyResponse toSurveyResponse(SurveyRequest.Submit request, Member member) {
        return SurveyResponse.builder()
                .member(member)
                .submissionType(request.submissionType())
                .birthYear(request.birthYear().shortValue())
                .gender(request.gender())
                .jobType(request.jobType())
                .menstrualCycleDays(request.menstrualCycleDays() == null ? null : request.menstrualCycleDays().shortValue())
                .lastPeriodStartedOn(request.lastPeriodStartedOn())
                .smokingStatus(request.smokingStatus())
                .drinkingStatus(request.drinkingStatus())
                .dietType(request.dietType())
                .exerciseFrequency(request.exerciseFrequency())
                .pregnant(request.pregnant())
                .underlyingDiseaseNone(request.underlyingDiseaseNone())
                .medicationNone(request.medicationNone())
                .allergyNone(request.allergyNone())
                .currentIngredientNone(request.currentIngredientNone())
                .build();
    }

    // 선택된 금기 조건 -> SurveyContraindicationSelection 목록
    public static List<SurveyContraindicationSelection> toContraindicationSelections(
            SurveyResponse surveyResponse, List<Contraindication> contraindications) {
        return contraindications.stream()
                .map(contraindication -> SurveyContraindicationSelection.builder()
                        .surveyResponse(surveyResponse)
                        .contraindication(contraindication)
                        .build())
                .toList();
    }

    // 온보딩 건강 고민 코드 -> SurveyOnboardingConcernSelection 목록
    public static List<SurveyOnboardingConcernSelection> toConcernSelections(
            SurveyResponse surveyResponse, List<OnboardingConcernCode> concernCodes) {
        return concernCodes.stream()
                .map(code -> SurveyOnboardingConcernSelection.builder()
                        .surveyResponse(surveyResponse)
                        .concernCode(code)
                        .build())
                .toList();
    }

    // 현재 복용 성분 -> SurveyCurrentIngredientSelection 목록
    public static List<SurveyCurrentIngredientSelection> toCurrentIngredientSelections(
            SurveyResponse surveyResponse, List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> SurveyCurrentIngredientSelection.builder()
                        .surveyResponse(surveyResponse)
                        .ingredient(ingredient)
                        .build())
                .toList();
    }

    // 저장 결과 -> 응답 DTO
    public static SurveyResult.Submit toSubmitResult(SurveyResponse surveyResponse, Recommendation recommendation) {
        return SurveyResult.Submit.builder()
                .surveyResponseId(surveyResponse.getId())
                .recommendationId(recommendation.getId())
                .recommendationStatus(recommendation.getStatus())
                .completedAt(surveyResponse.getCompletedAt())
                .build();
    }
}
