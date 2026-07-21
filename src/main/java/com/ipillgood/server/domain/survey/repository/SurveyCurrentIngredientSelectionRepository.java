package com.ipillgood.server.domain.survey.repository;

import com.ipillgood.server.domain.survey.entity.SurveyCurrentIngredientSelection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SurveyCurrentIngredientSelectionRepository extends JpaRepository<SurveyCurrentIngredientSelection, Long> {

    @Query("select s.ingredient.id from SurveyCurrentIngredientSelection s where s.surveyResponse.id = :surveyResponseId")
    List<Long> findIngredientIdsBySurveyResponseId(Long surveyResponseId);
}
