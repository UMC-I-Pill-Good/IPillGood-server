package com.ipillgood.server.domain.intake.service;

import com.ipillgood.server.domain.intake.entity.IntakeDay;
import com.ipillgood.server.domain.intake.repository.IntakeDayRepository;
import com.ipillgood.server.domain.intake.repository.IntakeRecordRepository;
import com.ipillgood.server.domain.intake.repository.MemberActiveProductRepository;
import com.ipillgood.server.domain.intake.repository.TodayIntakeRecordRow;
import com.ipillgood.server.domain.intake.repository.TodayScheduledProductRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodayIntakeCompletionService {

    private final IntakeDayRepository intakeDayRepository;
    private final IntakeRecordRepository intakeRecordRepository;
    private final MemberActiveProductRepository memberActiveProductRepository;

    @Transactional
    public void recalculateIfTodayExists(Long memberId, LocalDate currentDate, LocalDateTime completedAt) {
        IntakeDay intakeDay = intakeDayRepository.findByMemberIdAndIntakeOn(memberId, currentDate)
                .orElse(null);
        if (intakeDay == null) {
            return;
        }

        List<TodayScheduledProductRow> scheduledRows = findTodayScheduledRows(memberId, currentDate);
        if (scheduledRows.isEmpty()) {
            intakeDay.changeCompletion(false, completedAt);
            return;
        }

        Map<Long, TodayIntakeRecordRow> recordsByActiveProductId = findRecordsByActiveProductId(
                intakeDay,
                scheduledRows
        );
        int takenCount = (int) scheduledRows.stream()
                .map(row -> recordsByActiveProductId.get(row.activeProductId()))
                .filter(record -> record != null && Boolean.TRUE.equals(record.taken()))
                .count();

        intakeDay.changeCompletion(takenCount == scheduledRows.size(), completedAt);
    }

    private List<TodayScheduledProductRow> findTodayScheduledRows(Long memberId, LocalDate currentDate) {
        return memberActiveProductRepository
                .findTodayScheduleCandidateRows(memberId, currentDate)
                .stream()
                .filter(row -> isScheduledOn(row.scheduleAnchorOn(), row.frequencyIntervalDays(), currentDate))
                .toList();
    }

    private Map<Long, TodayIntakeRecordRow> findRecordsByActiveProductId(
            IntakeDay intakeDay,
            List<TodayScheduledProductRow> scheduledRows
    ) {
        List<Long> activeProductIds = scheduledRows.stream()
                .map(TodayScheduledProductRow::activeProductId)
                .toList();
        return intakeRecordRepository.findTodayRecordRows(intakeDay.getId(), activeProductIds)
                .stream()
                .collect(Collectors.toMap(
                        TodayIntakeRecordRow::activeProductId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    private boolean isScheduledOn(LocalDate scheduleAnchorOn, Short frequencyIntervalDays, LocalDate currentDate) {
        if (scheduleAnchorOn == null || frequencyIntervalDays == null || frequencyIntervalDays < 1) {
            return false;
        }

        long daysSinceAnchor = ChronoUnit.DAYS.between(scheduleAnchorOn, currentDate);
        return daysSinceAnchor >= 0 && daysSinceAnchor % frequencyIntervalDays == 0;
    }
}
