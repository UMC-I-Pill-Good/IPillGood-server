package com.ipillgood.server.domain.survey.repository;

import com.ipillgood.server.domain.survey.entity.SurveyContraindicationSelection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SurveyContraindicationSelectionRepository extends JpaRepository<SurveyContraindicationSelection, Long> {

    @Query("select s.contraindication.id from SurveyContraindicationSelection s where s.surveyResponse.id = :surveyResponseId")
    List<Long> findContraindicationIdsBySurveyResponseId(Long surveyResponseId);
}
