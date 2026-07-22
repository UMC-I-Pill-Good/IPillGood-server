package com.ipillgood.server.domain.intake.repository;

import com.ipillgood.server.domain.intake.entity.IntakeDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface IntakeDayRepository extends JpaRepository<IntakeDay, Long> {

    Optional<IntakeDay> findByMemberIdAndIntakeOn(Long memberId, LocalDate intakeOn);
}
