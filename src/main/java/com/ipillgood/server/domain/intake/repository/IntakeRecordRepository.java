package com.ipillgood.server.domain.intake.repository;

import com.ipillgood.server.domain.intake.entity.IntakeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface IntakeRecordRepository extends JpaRepository<IntakeRecord, Long> {

    @Query("""
            select new com.ipillgood.server.domain.intake.repository.TodayIntakeRecordRow(
                ap.id,
                ir.taken,
                ir.takenAt
            )
            from IntakeRecord ir
            join ir.memberActiveProduct ap
            where ir.intakeDay.id = :intakeDayId
              and ap.id in :activeProductIds
            """)
    List<TodayIntakeRecordRow> findTodayRecordRows(
            @Param("intakeDayId") Long intakeDayId,
            @Param("activeProductIds") Collection<Long> activeProductIds
    );

    @Query("""
            select ir
            from IntakeRecord ir
            join fetch ir.product p
            left join fetch ir.memberActiveProduct ap
            where ir.intakeDay.id = :intakeDayId
              and p.id in :productIds
            """)
    List<IntakeRecord> findTodayRecordEntities(
            @Param("intakeDayId") Long intakeDayId,
            @Param("productIds") Collection<Long> productIds
    );
}
