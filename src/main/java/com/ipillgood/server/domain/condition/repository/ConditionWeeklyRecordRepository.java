package com.ipillgood.server.domain.condition.repository;

import com.ipillgood.server.domain.condition.entity.ConditionWeeklyRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionWeeklyRecordRepository extends JpaRepository<ConditionWeeklyRecord, Long> {

    Optional<ConditionWeeklyRecord> findByMember_IdAndWeekStartOn(Long memberId, LocalDate weekStartOn);

    boolean existsByMember_IdAndWeekStartOn(Long memberId, LocalDate weekStartOn);

    // 조회 월과 주간 기간이 겹치는 기록(월을 걸치는 주간 기록 포함)
    List<ConditionWeeklyRecord> findByMember_IdAndWeekEndOnGreaterThanEqualAndWeekStartOnLessThanEqualOrderByWeekStartOnAsc(
            Long memberId, LocalDate monthStart, LocalDate monthEnd);
}
