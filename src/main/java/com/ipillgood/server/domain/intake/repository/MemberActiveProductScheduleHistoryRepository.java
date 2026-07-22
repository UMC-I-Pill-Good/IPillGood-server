package com.ipillgood.server.domain.intake.repository;

import com.ipillgood.server.domain.intake.entity.MemberActiveProductScheduleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberActiveProductScheduleHistoryRepository
        extends JpaRepository<MemberActiveProductScheduleHistory, Long> {
}
