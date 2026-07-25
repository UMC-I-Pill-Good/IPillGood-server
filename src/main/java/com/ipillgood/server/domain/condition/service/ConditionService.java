package com.ipillgood.server.domain.condition.service;

import com.ipillgood.server.domain.condition.code.ConditionErrorCode;
import com.ipillgood.server.domain.condition.converter.ConditionConverter;
import com.ipillgood.server.domain.condition.dto.ConditionRequest;
import com.ipillgood.server.domain.condition.dto.ConditionResponse;
import com.ipillgood.server.domain.condition.entity.ConditionPopupLog;
import com.ipillgood.server.domain.condition.entity.ConditionWeeklyRecord;
import com.ipillgood.server.domain.condition.exception.ConditionException;
import com.ipillgood.server.domain.condition.repository.ConditionPopupLogRepository;
import com.ipillgood.server.domain.condition.repository.ConditionWeeklyRecordRepository;
import com.ipillgood.server.domain.intake.service.IntakeService;
import com.ipillgood.server.domain.intake.service.IntakeWeeklyCompletionSummary;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConditionService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final int WEEK_DAYS = 7;
    private static final int MAX_SCORE = 5;
    private static final int AVERAGE_SCALE = 1;

    private final ConditionWeeklyRecordRepository conditionWeeklyRecordRepository;
    private final ConditionPopupLogRepository conditionPopupLogRepository;
    private final MemberRepository memberRepository;
    private final IntakeService intakeService;

    public ConditionResponse.CurrentWeek getCurrentWeek(Long memberId) {
        validateOnboardingCompleted(getMember(memberId));

        LocalDate today = currentDate();
        LocalDate weekStartOn = weekStartOn(today);
        LocalDate weekEndOn = weekStartOn.plusDays(WEEK_DAYS - 1);
        boolean isSunday = today.getDayOfWeek() == DayOfWeek.SUNDAY;

        ConditionWeeklyRecord record = conditionWeeklyRecordRepository
                .findByMember_IdAndWeekStartOn(memberId, weekStartOn)
                .orElse(null);
        boolean checked = record != null;
        boolean checkAvailable = isSunday && !checked;

        ConditionPopupLog popupLog = conditionPopupLogRepository
                .findByMember_IdAndWeekStartOn(memberId, weekStartOn)
                .orElse(null);
        boolean autoPopupAvailable = isSunday && !checked && (popupLog == null || popupLog.getAutoShownAt() == null);
        boolean sundayIntakeWarningRequired = isSunday && !checked && intakeService.hasIncompleteTodayIntake(memberId);

        return ConditionConverter.toCurrentWeek(
                today, weekStartOn, weekEndOn, isSunday, checkAvailable,
                record, popupLog, autoPopupAvailable, sundayIntakeWarningRequired);
    }

    @Transactional
    public ConditionResponse.Detail saveWeeklyRecord(Long memberId, ConditionRequest.SaveWeeklyRecord request) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);
        validateSaveWeeklyRecordRequest(request);

        LocalDate today = currentDate();
        if (today.getDayOfWeek() != DayOfWeek.SUNDAY) {
            throw new ConditionException(ConditionErrorCode.NOT_SUNDAY);
        }

        LocalDate weekStartOn = weekStartOn(today);
        LocalDate weekEndOn = weekStartOn.plusDays(WEEK_DAYS - 1);
        if (conditionWeeklyRecordRepository.existsByMember_IdAndWeekStartOn(memberId, weekStartOn)) {
            throw new ConditionException(ConditionErrorCode.ALREADY_CHECKED);
        }

        short sleepScore = calculateSleepScore(request.sleepHours());
        IntakeWeeklyCompletionSummary completionSummary =
                intakeService.getWeeklyCompletionSummary(memberId, weekStartOn, weekEndOn);
        short intakeDaysCount = (short) completionSummary.completedDays();
        short intakeScore = (short) Math.min(WEEK_DAYS - completionSummary.missedDays(), MAX_SCORE);
        BigDecimal conditionScore = calculateConditionScore(request.vitalityScore(), sleepScore, intakeScore);

        ConditionWeeklyRecord record = ConditionWeeklyRecord.builder()
                .member(member)
                .weekStartOn(weekStartOn)
                .weekEndOn(weekEndOn)
                .checkedOn(today)
                .vitalityScore(request.vitalityScore().shortValue())
                .sleepHours(request.sleepHours().shortValue())
                .sleepMinutes(request.sleepMinutes().shortValue())
                .sleepScore(sleepScore)
                .intakeDaysCount(intakeDaysCount)
                .intakeScore(intakeScore)
                .conditionScore(conditionScore)
                .build();

        try {
            // saveAndFlush로 즉시 INSERT를 실행해, 동시 요청으로 유니크 제약(member_id, week_start_on)을 위반하는 경우
            // 커밋 시점이 아니라 여기서 바로 DataIntegrityViolationException을 잡아 409로 변환한다.
            conditionWeeklyRecordRepository.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            throw new ConditionException(ConditionErrorCode.ALREADY_CHECKED);
        }

        return ConditionConverter.toDetail(record);
    }

    public ConditionResponse.MonthlySummary getMonthlyRecords(Long memberId, String year, String month) {
        validateOnboardingCompleted(getMember(memberId));

        YearMonth targetMonth = validateMonthlyPeriod(year, month);
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        List<ConditionWeeklyRecord> records = conditionWeeklyRecordRepository
                .findByMember_IdAndWeekEndOnGreaterThanEqualAndWeekStartOnLessThanEqualOrderByWeekStartOnAsc(
                        memberId, monthStart, monthEnd);

        return ConditionConverter.toMonthlySummary(
                targetMonth.getYear(),
                targetMonth.getMonthValue(),
                averageConditionScore(records),
                averageVitalityScore(records),
                averageSleepHours(records),
                averageIntakeDaysCount(records),
                records);
    }

    @Transactional
    public ConditionResponse.PopupAutoShown recordPopupAutoShown(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);
        LocalDate weekStartOn = validatePopupTarget(memberId);

        ConditionPopupLog popupLog = conditionPopupLogRepository
                .findByMember_IdAndWeekStartOn(memberId, weekStartOn)
                .orElseGet(() -> ConditionPopupLog.create(member, weekStartOn));
        popupLog.markAutoShown(currentDateTime());
        conditionPopupLogRepository.save(popupLog);

        return ConditionConverter.toPopupAutoShown(popupLog);
    }

    @Transactional
    public ConditionResponse.PopupDismissed recordPopupDismissed(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);
        LocalDate weekStartOn = validatePopupTarget(memberId);

        ConditionPopupLog popupLog = conditionPopupLogRepository
                .findByMember_IdAndWeekStartOn(memberId, weekStartOn)
                .orElseGet(() -> ConditionPopupLog.create(member, weekStartOn));
        popupLog.markDismissed(currentDateTime());
        conditionPopupLogRepository.save(popupLog);

        return ConditionConverter.toPopupDismissed(popupLog);
    }

    // 컨디션 체크 팝업은 일요일에, 아직 이번 주 체크를 완료하지 않았을 때만 노출된다
    private LocalDate validatePopupTarget(Long memberId) {
        LocalDate today = currentDate();
        if (today.getDayOfWeek() != DayOfWeek.SUNDAY) {
            throw new ConditionException(ConditionErrorCode.POPUP_NOT_SUNDAY);
        }

        LocalDate weekStartOn = weekStartOn(today);
        if (conditionWeeklyRecordRepository.existsByMember_IdAndWeekStartOn(memberId, weekStartOn)) {
            throw new ConditionException(ConditionErrorCode.ALREADY_CHECKED);
        }
        return weekStartOn;
    }

    public ConditionResponse.Detail getWeeklyRecordDetail(Long memberId, Long recordId) {
        validateOnboardingCompleted(getMember(memberId));

        ConditionWeeklyRecord record = conditionWeeklyRecordRepository.findById(recordId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        if (!record.getMember().getId().equals(memberId)) {
            throw new ConditionException(ConditionErrorCode.FORBIDDEN);
        }

        return ConditionConverter.toDetail(record);
    }

    // 7시간 이상(5점) / 6시간대(4점) / 5시간대(3점) / 4시간대(2점) / 4시간 미만(1점)
    private short calculateSleepScore(int sleepHours) {
        if (sleepHours >= 7) {
            return 5;
        }
        if (sleepHours == 6) {
            return 4;
        }
        if (sleepHours == 5) {
            return 3;
        }
        if (sleepHours == 4) {
            return 2;
        }
        return 1;
    }

    // 최종 컨디션 점수 = (활력 점수 + 수면 점수 + 영양제 섭취 점수) / 3
    private BigDecimal calculateConditionScore(int vitalityScore, short sleepScore, short intakeScore) {
        BigDecimal sum = BigDecimal.valueOf(vitalityScore + sleepScore + intakeScore);
        return sum.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal averageConditionScore(List<ConditionWeeklyRecord> records) {
        if (records.isEmpty()) {
            return null;
        }
        BigDecimal sum = records.stream()
                .map(ConditionWeeklyRecord::getConditionScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(records.size()), AVERAGE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal averageVitalityScore(List<ConditionWeeklyRecord> records) {
        if (records.isEmpty()) {
            return null;
        }
        int sum = records.stream().mapToInt(ConditionWeeklyRecord::getVitalityScore).sum();
        return BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(records.size()), AVERAGE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal averageSleepHours(List<ConditionWeeklyRecord> records) {
        if (records.isEmpty()) {
            return null;
        }
        BigDecimal totalHours = records.stream()
                .map(record -> BigDecimal.valueOf(record.getSleepHours())
                        .add(BigDecimal.valueOf(record.getSleepMinutes())
                                .divide(BigDecimal.valueOf(60), 10, RoundingMode.HALF_UP)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalHours.divide(BigDecimal.valueOf(records.size()), AVERAGE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal averageIntakeDaysCount(List<ConditionWeeklyRecord> records) {
        if (records.isEmpty()) {
            return null;
        }
        int sum = records.stream().mapToInt(ConditionWeeklyRecord::getIntakeDaysCount).sum();
        return BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(records.size()), AVERAGE_SCALE, RoundingMode.HALF_UP);
    }

    private void validateSaveWeeklyRecordRequest(ConditionRequest.SaveWeeklyRecord request) {
        if (request == null
                || request.vitalityScore() == null || request.vitalityScore() < 1 || request.vitalityScore() > 5
                || request.sleepHours() == null || request.sleepHours() < 0 || request.sleepHours() > 23
                || request.sleepMinutes() == null || request.sleepMinutes() < 0 || request.sleepMinutes() > 59) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
    }

    private YearMonth validateMonthlyPeriod(String year, String month) {
        int parsedYear = parseMonthlyInteger(year);
        int parsedMonth = parseMonthlyInteger(month);
        if (parsedYear < 1 || parsedMonth < 1 || parsedMonth > 12) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }

        try {
            return YearMonth.of(parsedYear, parsedMonth);
        } catch (DateTimeException e) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
    }

    private int parseMonthlyInteger(String value) {
        if (value == null || value.isBlank()) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
    }

    private LocalDate weekStartOn(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    private LocalDate currentDate() {
        return LocalDate.now(SERVICE_ZONE_ID);
    }

    private LocalDateTime currentDateTime() {
        return LocalDateTime.now(SERVICE_ZONE_ID);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.UNAUTHORIZED));
    }

    private void validateOnboardingCompleted(Member member) {
        if (member.getOnboardingCompletedAt() == null) {
            throw new ConditionException(ConditionErrorCode.ONBOARDING_NOT_COMPLETED);
        }
    }
}
