package com.ipillgood.server.domain.survey.dto;

import com.ipillgood.server.domain.survey.entity.enums.DietType;
import com.ipillgood.server.domain.survey.entity.enums.DrinkingStatus;
import com.ipillgood.server.domain.survey.entity.enums.ExerciseFrequency;
import com.ipillgood.server.domain.survey.entity.enums.JobType;
import com.ipillgood.server.domain.survey.entity.enums.OnboardingConcernCode;
import com.ipillgood.server.domain.survey.entity.enums.SmokingStatus;
import com.ipillgood.server.domain.survey.entity.enums.SurveySubmissionType;
import com.ipillgood.server.global.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;

public class SurveyRequest {

    // 설문 응답 저장 및 추천 생성 시작 요청 (화면1~5)
    public record Submit(
            @NotNull(message = "설문 제출 유형을 선택해주세요.")
            SurveySubmissionType submissionType,

            @NotNull(message = "출생연도를 입력해주세요.")
            @Min(value = 1900, message = "1900년 이후 연도를 입력해주세요.")
            Integer birthYear,

            @NotNull(message = "성별을 선택해주세요.")
            Gender gender,

            @NotNull(message = "직군을 선택해주세요.")
            JobType jobType,

            @Min(value = 1, message = "1~50 사이의 값을 입력해주세요.")
            @Max(value = 50, message = "1~50 사이의 값을 입력해주세요.")
            Integer menstrualCycleDays,

            @PastOrPresent(message = "미래 날짜는 입력할 수 없습니다.")
            LocalDate lastPeriodStartedOn,

            @NotNull(message = "흡연 여부를 선택해주세요.")
            SmokingStatus smokingStatus,

            @NotNull(message = "음주 여부를 선택해주세요.")
            DrinkingStatus drinkingStatus,

            @NotNull(message = "식습관을 선택해주세요.")
            DietType dietType,

            @NotNull(message = "운동 빈도를 선택해주세요.")
            ExerciseFrequency exerciseFrequency,

            Boolean pregnant,

            @NotNull(message = "기저질환 없음 여부를 입력해주세요.")
            Boolean underlyingDiseaseNone,

            @NotNull(message = "복용약 없음 여부를 입력해주세요.")
            Boolean medicationNone,

            @NotNull(message = "알러지 없음 여부를 입력해주세요.")
            Boolean allergyNone,

            @NotNull(message = "현재 복용 성분 없음 여부를 입력해주세요.")
            Boolean currentIngredientNone,

            @NotNull(message = "선택한 금기 조건 목록이 필요합니다.")
            List<Long> contraindicationIds,

            @NotNull(message = "건강 고민은 1~3개까지 선택할 수 있습니다.")
            List<OnboardingConcernCode> onboardingConcernCodes,

            @NotNull(message = "현재 복용 중인 성분 목록이 필요합니다.")
            List<Long> currentIngredientIds
    ) {
    }
}
