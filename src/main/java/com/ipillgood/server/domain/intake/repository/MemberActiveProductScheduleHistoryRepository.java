package com.ipillgood.server.domain.intake.repository;

import com.ipillgood.server.domain.intake.entity.MemberActiveProductScheduleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberActiveProductScheduleHistoryRepository
        extends JpaRepository<MemberActiveProductScheduleHistory, Long> {

    @Query("""
            select history
            from MemberActiveProductScheduleHistory history
            where history.memberActiveProduct.id = :activeProductId
              and history.effectiveTo is null
            """)
    Optional<MemberActiveProductScheduleHistory> findActiveByActiveProductId(
            @Param("activeProductId") Long activeProductId
    );
}
