package com.ipillgood.server.domain.survey.repository;

import com.ipillgood.server.domain.survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
}
