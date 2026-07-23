package com.ipillgood.server.domain.condition.converter;

import com.ipillgood.server.domain.condition.dto.ConditionResponse;
import com.ipillgood.server.domain.condition.entity.ConditionPopupLog;
import com.ipillgood.server.domain.condition.entity.ConditionWeeklyRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ConditionConverter {

    public static ConditionResponse.CurrentWeek toCurrentWeek(
            LocalDate today,
            LocalDate weekStartOn,
            LocalDate weekEndOn,
            boolean isSunday,
            boolean checkAvailable,
            ConditionWeeklyRecord record,
            ConditionPopupLog popupLog,
            boolean autoPopupAvailable,
            boolean sundayIntakeWarningRequired
    ) {
        return ConditionResponse.CurrentWeek.builder()
                .today(today)
                .weekStartOn(weekStartOn)
                .weekEndOn(weekEndOn)
                .isSunday(isSunday)
                .checkAvailable(checkAvailable)
                .checked(record != null)
                .recordId(record == null ? null : record.getId())
                .autoPopupAvailable(autoPopupAvailable)
                .autoShownAt(popupLog == null ? null : popupLog.getAutoShownAt())
                .dismissedAt(popupLog == null ? null : popupLog.getDismissedAt())
                .sundayIntakeWarningRequired(sundayIntakeWarningRequired)
                .build();
    }

    public static ConditionResponse.Detail toDetail(ConditionWeeklyRecord record) {
        return ConditionResponse.Detail.builder()
                .recordId(record.getId())
                .weekStartOn(record.getWeekStartOn())
                .weekEndOn(record.getWeekEndOn())
                .checkedOn(record.getCheckedOn())
                .vitalityScore(record.getVitalityScore().intValue())
                .sleepHours(record.getSleepHours().intValue())
                .sleepMinutes(record.getSleepMinutes().intValue())
                .sleepScore(record.getSleepScore().intValue())
                .intakeDaysCount(record.getIntakeDaysCount().intValue())
                .intakeScore(record.getIntakeScore().intValue())
                .conditionScore(record.getConditionScore())
                .build();
    }

    public static ConditionResponse.WeeklySummary toWeeklySummary(ConditionWeeklyRecord record) {
        return ConditionResponse.WeeklySummary.builder()
                .recordId(record.getId())
                .weekStartOn(record.getWeekStartOn())
                .weekEndOn(record.getWeekEndOn())
                .conditionScore(record.getConditionScore())
                .build();
    }

    public static ConditionResponse.MonthlySummary toMonthlySummary(
            int year,
            int month,
            BigDecimal averageConditionScore,
            BigDecimal averageVitalityScore,
            BigDecimal averageSleepHours,
            BigDecimal averageIntakeDaysCount,
            List<ConditionWeeklyRecord> records
    ) {
        return ConditionResponse.MonthlySummary.builder()
                .year(year)
                .month(month)
                .averageConditionScore(averageConditionScore)
                .averageVitalityScore(averageVitalityScore)
                .averageSleepHours(averageSleepHours)
                .averageIntakeDaysCount(averageIntakeDaysCount)
                .records(records.stream().map(ConditionConverter::toWeeklySummary).toList())
                .build();
    }
}
