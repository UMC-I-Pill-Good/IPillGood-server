package com.ipillgood.server.domain.survey.repository;

import com.ipillgood.server.domain.survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    
    @Query("""
            select sr
            from SurveyResponse sr
            where sr.member.id in :memberIds
              and sr.completedAt is not null
              and sr.createdAt = (
                  select max(latest.createdAt)
                  from SurveyResponse latest
                  where latest.member = sr.member
                    and latest.completedAt is not null
              )
            """)
    List<SurveyResponse> findLatestCompletedByMemberIds(@Param("memberIds") Collection<Long> memberIds);
}
