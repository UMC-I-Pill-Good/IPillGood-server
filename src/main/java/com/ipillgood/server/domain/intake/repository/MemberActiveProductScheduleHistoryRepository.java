package com.ipillgood.server.domain.intake.repository;

import com.ipillgood.server.domain.intake.entity.MemberActiveProductScheduleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
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

    @Query("""
            select new com.ipillgood.server.domain.intake.repository.CalendarScheduleHistoryRow(
                ap.id,
                ap.startedOn,
                ap.stoppedOn,
                history.scheduleAnchorOn,
                history.frequencyIntervalDays,
                history.effectiveFrom,
                history.effectiveTo
            )
            from MemberActiveProductScheduleHistory history
            join history.memberActiveProduct ap
            where ap.member.id = :memberId
              and ap.startedOn <= :endDate
              and (ap.stoppedOn is null or :startDate < ap.stoppedOn)
              and history.effectiveFrom <= :endDate
              and (history.effectiveTo is null or :startDate < history.effectiveTo)
            order by ap.id asc, history.effectiveFrom asc, history.id asc
            """)
    List<CalendarScheduleHistoryRow> findCalendarScheduleHistoryRows(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select new com.ipillgood.server.domain.intake.repository.CalendarScheduleHistoryRow(
                ap.id,
                ap.startedOn,
                ap.stoppedOn,
                history.scheduleAnchorOn,
                history.frequencyIntervalDays,
                history.effectiveFrom,
                history.effectiveTo
            )
            from MemberActiveProductScheduleHistory history
            join history.memberActiveProduct ap
            where ap.member.id = :memberId
              and ap.startedOn <= :currentDate
              and history.effectiveFrom <= :currentDate
            order by ap.startedOn asc, ap.id asc, history.effectiveFrom asc, history.id asc
            """)
    List<CalendarScheduleHistoryRow> findStreakScheduleHistoryRows(
            @Param("memberId") Long memberId,
            @Param("currentDate") LocalDate currentDate
    );
}
