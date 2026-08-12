package com.ipillgood.server.domain.recommendation.service;

import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.survey.entity.SurveyResponse;
import com.ipillgood.server.domain.survey.entity.enums.OnboardingConcernCode;
import java.time.Year;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

// 설문 응답 + 안전 후보 성분 목록 -> Gemini 프롬프트 텍스트 조립
@Component
public class RecommendationPromptBuilder {

    public String build(SurveyResponse surveyResponse, List<OnboardingConcernCode> concernCodes,
                         List<Ingredient> currentIngredients, List<Ingredient> candidates,
                         Map<Long, List<String>> effectsByIngredientId) {

        int age = Year.now().getValue() - surveyResponse.getBirthYear() + 1;

        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 영양제 추천 전문가입니다. 아래 사용자 정보와 안전 후보 성분 목록을 참고하여, ")
                .append("사용자에게 가장 적합한 영양성분을 최대 3개까지 추천해 주세요.\n\n");

        prompt.append("[사용자 정보]\n")
                .append("- 나이: 약 ").append(age).append("세\n")
                .append("- 성별: ").append(surveyResponse.getGender().getLabel()).append("\n")
                .append("- 직군: ").append(surveyResponse.getJobType().getLabel()).append("\n")
                .append("- 흡연: ").append(surveyResponse.getSmokingStatus().getLabel()).append("\n")
                .append("- 음주: ").append(surveyResponse.getDrinkingStatus().getLabel()).append("\n")
                .append("- 식습관: ").append(surveyResponse.getDietType().getLabel()).append("\n")
                .append("- 운동 빈도: ").append(surveyResponse.getExerciseFrequency().getLabel()).append("\n");

        if (surveyResponse.getPregnant() != null) {
            prompt.append("- 임신 여부: ").append(surveyResponse.getPregnant() ? "임신 중" : "임신 아님").append("\n");
        }

        prompt.append("\n[관심 건강 고민]\n");
        concernCodes.forEach(code -> prompt.append("- ").append(code.getLabel()).append("\n"));

        prompt.append("\n[현재 복용 중인 성분]\n");
        if (currentIngredients.isEmpty()) {
            prompt.append("- 없음\n");
        } else {
            currentIngredients.forEach(ingredient -> prompt.append("- ").append(ingredient.getName()).append("\n"));
        }

        prompt.append("\n[추천 가능한 안전 후보 성분 목록 (아래 ingredientId 중에서만 선택해야 합니다)]\n");
        candidates.forEach(ingredient -> {
            List<String> effects = effectsByIngredientId.getOrDefault(ingredient.getId(), List.of());
            prompt.append("- ingredientId: ").append(ingredient.getId())
                    .append(", 이름: ").append(ingredient.getName())
                    .append(", 효능: ").append(String.join(", ", effects))
                    .append("\n");
        });

        prompt.append("\n[출력 규칙]\n")
                .append("- healthSummary에는 사용자의 현재 건강 상태를 2~3줄, 약 100자 이내로 간결하게 요약해 주세요.\n")
                .append("- recommendations는 위 후보 목록에 있는 ingredientId만 사용해 최대 3개까지 반환해 주세요.\n")
                .append("- 각 추천 항목에는 반드시 구체적인 aiReason(추천 이유)을 채워야 합니다. 이유가 없는 성분은 추천하지 마세요.\n");

        return prompt.toString();
    }
}
