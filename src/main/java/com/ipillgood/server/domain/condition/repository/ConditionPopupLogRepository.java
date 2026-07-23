package com.ipillgood.server.domain.condition.repository;

import com.ipillgood.server.domain.condition.entity.ConditionPopupLog;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionPopupLogRepository extends JpaRepository<ConditionPopupLog, Long> {

    Optional<ConditionPopupLog> findByMember_IdAndWeekStartOn(Long memberId, LocalDate weekStartOn);
}
