package com.ipillgood.server.domain.survey.repository;

import com.ipillgood.server.domain.survey.entity.SurveyOnboardingConcernSelection;
import com.ipillgood.server.domain.survey.entity.enums.OnboardingConcernCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SurveyOnboardingConcernSelectionRepository extends JpaRepository<SurveyOnboardingConcernSelection, Long> {

    @Query("select s.concernCode from SurveyOnboardingConcernSelection s where s.surveyResponse.id = :surveyResponseId")
    List<OnboardingConcernCode> findConcernCodesBySurveyResponseId(Long surveyResponseId);
}
