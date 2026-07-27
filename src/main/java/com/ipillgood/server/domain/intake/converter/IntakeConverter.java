package com.ipillgood.server.domain.intake.converter;

import com.ipillgood.server.domain.cabinet.entity.MemberProduct;
import com.ipillgood.server.domain.intake.dto.IntakeResponse;
import com.ipillgood.server.domain.intake.entity.IntakeRecord;
import com.ipillgood.server.domain.intake.entity.MemberActiveProduct;
import com.ipillgood.server.domain.intake.entity.enums.IntakeMascotStage;
import com.ipillgood.server.domain.intake.entity.enums.IntakeStreakStatus;
import com.ipillgood.server.domain.intake.repository.ActiveProductRow;
import com.ipillgood.server.domain.intake.repository.ActiveProductSettingsRow;
import com.ipillgood.server.domain.intake.repository.CompatibilityConflictRow;
import com.ipillgood.server.domain.intake.repository.DailyTakenProductRow;
import com.ipillgood.server.domain.intake.repository.TodayIntakeRecordRow;
import com.ipillgood.server.domain.intake.repository.TodayScheduledProductRow;
import com.ipillgood.server.domain.product.entity.Product;
import com.ipillgood.server.global.util.EtcProductImageKeyResolver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class IntakeConverter {

    private static final DateTimeFormatter INTAKE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private IntakeConverter() {
    }

    public static IntakeResponse.ActiveProducts toActiveProducts(
            List<ActiveProductRow> rows,
            Function<String, String> imageUrlResolver
    ) {
        List<IntakeResponse.ActiveProductSummary> activeProducts = rows.stream()
                .map(row -> toActiveProductSummary(row, imageUrlResolver))
                .toList();

        return IntakeResponse.ActiveProducts.builder()
                .totalCount(activeProducts.size())
                .activeProducts(activeProducts)
                .build();
    }

    public static IntakeResponse.Calendar toCalendar(
            YearMonth yearMonth,
            List<IntakeResponse.CalendarDay> days
    ) {
        return IntakeResponse.Calendar.builder()
                .year(yearMonth.getYear())
                .month(yearMonth.getMonthValue())
                .days(days)
                .build();
    }

    public static IntakeResponse.CalendarDay toCalendarDay(
            LocalDate date,
            boolean allCompleted,
            IntakeStreakStatus streakStatus,
            int takenCount,
            LocalDateTime completedAt
    ) {
        boolean hasTakenRecords = takenCount > 0;

        return IntakeResponse.CalendarDay.builder()
                .date(date)
                .dayOfMonth(date.getDayOfMonth())
                .hasTakenRecords(hasTakenRecords)
                .allCompleted(allCompleted)
                .streakStatus(streakStatus)
                .streakIncluded(isStreakIncluded(streakStatus))
                .selectable(hasTakenRecords)
                .takenCount(takenCount)
                .completedAt(completedAt)
                .build();
    }

    public static IntakeResponse.DailyTakenProducts toDailyTakenProducts(
            LocalDate date,
            List<DailyTakenProductRow> rows
    ) {
        List<IntakeResponse.DailyTakenProduct> products = rows.stream()
                .map(IntakeConverter::toDailyTakenProduct)
                .toList();

        return IntakeResponse.DailyTakenProducts.builder()
                .date(date)
                .takenCount(products.size())
                .products(products)
                .build();
    }

    public static IntakeResponse.IntakeStreak toIntakeStreak(
            LocalDate currentDate,
            IntakeStreakStatus currentDateStreakStatus,
            int streakDays,
            int activeProductCount,
            LocalDate lastRoutineDate
    ) {
        IntakeMascotStage mascotStage = IntakeMascotStage.fromStreakDays(streakDays);

        return IntakeResponse.IntakeStreak.builder()
                .currentDate(currentDate)
                .currentDateStreakStatus(currentDateStreakStatus)
                .streakDays(streakDays)
                .mascotStage(mascotStage)
                .mascotStageLabel(mascotStage.getLabel())
                .activeProductCount(activeProductCount)
                .lastRoutineDate(lastRoutineDate)
                .nextStageThresholdDays(mascotStage.getNextStageThresholdDays())
                .build();
    }

    public static IntakeResponse.TodayIntakeStatus toTodayIntakeStatus(
            LocalDate currentDate,
            List<TodayScheduledProductRow> scheduledRows,
            Map<Long, TodayIntakeRecordRow> recordsByActiveProductId,
            boolean autoPopupShown
    ) {
        List<IntakeResponse.TodayScheduledProduct> scheduledProducts = scheduledRows.stream()
                .map(row -> toTodayScheduledProduct(row, recordsByActiveProductId.get(row.activeProductId())))
                .toList();

        int scheduledCount = scheduledProducts.size();
        int takenCount = (int) scheduledProducts.stream()
                .filter(product -> Boolean.TRUE.equals(product.taken()))
                .count();
        boolean allCompleted = scheduledCount > 0 && takenCount == scheduledCount;
        boolean missedNoticeVisible = scheduledCount > 0 && !allCompleted;
        boolean autoPopupRequired = missedNoticeVisible && !autoPopupShown;

        return IntakeResponse.TodayIntakeStatus.builder()
                .currentDate(currentDate)
                .scheduledCount(scheduledCount)
                .takenCount(takenCount)
                .allCompleted(allCompleted)
                .missedNoticeVisible(missedNoticeVisible)
                .autoPopupShown(autoPopupShown)
                .autoPopupRequired(autoPopupRequired)
                .scheduledProducts(scheduledProducts)
                .build();
    }

    public static IntakeResponse.TodayPopupShown toTodayPopupShown(
            LocalDate currentDate,
            LocalDateTime autoPopupShownAt
    ) {
        return IntakeResponse.TodayPopupShown.builder()
                .currentDate(currentDate)
                .autoPopupShown(autoPopupShownAt != null)
                .autoPopupShownAt(autoPopupShownAt)
                .build();
    }

    public static IntakeResponse.SaveTodayIntakeRecords toSaveTodayIntakeRecords(
            LocalDate currentDate,
            List<TodayScheduledProductRow> scheduledRows,
            Map<Long, IntakeRecord> recordsByProductId,
            LocalDateTime completedAt
    ) {
        List<IntakeResponse.TodayIntakeRecord> records = scheduledRows.stream()
                .map(row -> toTodayIntakeRecord(row, recordsByProductId.get(row.productId())))
                .toList();

        int scheduledCount = records.size();
        int takenCount = (int) records.stream()
                .filter(record -> Boolean.TRUE.equals(record.taken()))
                .count();
        boolean allCompleted = scheduledCount > 0 && takenCount == scheduledCount;

        return IntakeResponse.SaveTodayIntakeRecords.builder()
                .currentDate(currentDate)
                .scheduledCount(scheduledCount)
                .takenCount(takenCount)
                .allCompleted(allCompleted)
                .completedAt(completedAt)
                .missedNoticeVisible(scheduledCount > 0 && !allCompleted)
                .records(records)
                .build();
    }

    public static IntakeResponse.RegisterActiveProduct toRegisterActiveProduct(
            MemberActiveProduct activeProduct,
            ActiveProductRow row,
            Function<String, String> imageUrlResolver
    ) {
        return IntakeResponse.RegisterActiveProduct.builder()
                .activeProductId(row.activeProductId())
                .memberProductId(row.memberProductId())
                .productId(row.productId())
                .productName(row.productName())
                .thumbnailImageUrl(toThumbnailImageUrl(row, imageUrlResolver))
                .notificationEnabled(activeProduct.isNotificationEnabled())
                .intakeTime(activeProduct.getIntakeTime().format(INTAKE_TIME_FORMATTER))
                .frequency(activeProduct.getFrequency().name())
                .frequencyLabel(activeProduct.getFrequency().getLabel())
                .build();
    }

    public static IntakeResponse.UpdateActiveProductSettings toUpdateActiveProductSettings(
            ActiveProductSettingsRow row,
            LocalDate currentDate,
            Function<String, String> imageUrlResolver
    ) {
        return IntakeResponse.UpdateActiveProductSettings.builder()
                .activeProductId(row.activeProductId())
                .memberProductId(row.memberProductId())
                .productId(row.productId())
                .brand(row.brand())
                .productName(row.productName())
                .thumbnailImageUrl(toThumbnailImageUrl(
                        row.productId(),
                        row.ingredientCount(),
                        row.singleIngredientImageKey(),
                        imageUrlResolver
                ))
                .startedOn(row.startedOn())
                .intakeDayCount(toIntakeDayCount(row.startedOn(), currentDate))
                .notificationEnabled(row.notificationEnabled())
                .intakeTime(row.intakeTime() == null ? null : row.intakeTime().format(INTAKE_TIME_FORMATTER))
                .frequency(row.frequency() == null ? null : row.frequency().name())
                .frequencyLabel(row.frequency() == null ? null : row.frequency().getLabel())
                .frequencyIntervalDays(row.frequencyIntervalDays() == null ? null : row.frequencyIntervalDays().intValue())
                .scheduleAnchorOn(row.scheduleAnchorOn())
                .build();
    }

    public static IntakeResponse.RemoveActiveProduct toRemoveActiveProduct(
            MemberActiveProduct activeProduct,
            LocalDate stoppedOn
    ) {
        MemberProduct memberProduct = activeProduct.getMemberProduct();
        Product product = memberProduct.getProduct();

        return IntakeResponse.RemoveActiveProduct.builder()
                .activeProductId(activeProduct.getId())
                .memberProductId(memberProduct.getId())
                .productId(product.getId())
                .productName(product.getName())
                .stoppedOn(stoppedOn)
                .build();
    }

    public static IntakeResponse.CompatibilityCheck toCompatibilityCheck(
            List<CompatibilityConflictRow> rows
    ) {
        List<IntakeResponse.CompatibilityConflict> conflicts = rows.stream()
                .map(IntakeConverter::toCompatibilityConflict)
                .toList();

        return IntakeResponse.CompatibilityCheck.builder()
                .hasConflicts(!conflicts.isEmpty())
                .conflicts(conflicts)
                .build();
    }

    private static IntakeResponse.ActiveProductSummary toActiveProductSummary(
            ActiveProductRow row,
            Function<String, String> imageUrlResolver
    ) {
        return IntakeResponse.ActiveProductSummary.builder()
                .activeProductId(row.activeProductId())
                .memberProductId(row.memberProductId())
                .productId(row.productId())
                .productName(row.productName())
                .thumbnailImageUrl(toThumbnailImageUrl(row, imageUrlResolver))
                .build();
    }

    private static IntakeResponse.TodayScheduledProduct toTodayScheduledProduct(
            TodayScheduledProductRow row,
            TodayIntakeRecordRow record
    ) {
        boolean taken = record != null && Boolean.TRUE.equals(record.taken());

        return IntakeResponse.TodayScheduledProduct.builder()
                .activeProductId(row.activeProductId())
                .memberProductId(row.memberProductId())
                .productId(row.productId())
                .productName(row.productName())
                .taken(taken)
                .takenAt(taken ? record.takenAt() : null)
                .build();
    }

    private static IntakeResponse.DailyTakenProduct toDailyTakenProduct(
            DailyTakenProductRow row
    ) {
        return IntakeResponse.DailyTakenProduct.builder()
                .activeProductId(row.activeProductId())
                .productId(row.productId())
                .productName(row.productName())
                .takenAt(row.takenAt())
                .build();
    }

    private static IntakeResponse.TodayIntakeRecord toTodayIntakeRecord(
            TodayScheduledProductRow row,
            IntakeRecord record
    ) {
        boolean taken = record != null && record.isTaken();

        return IntakeResponse.TodayIntakeRecord.builder()
                .activeProductId(row.activeProductId())
                .productId(row.productId())
                .productName(row.productName())
                .scheduled(true)
                .taken(taken)
                .takenAt(taken ? record.getTakenAt() : null)
                .build();
    }

    private static IntakeResponse.CompatibilityConflict toCompatibilityConflict(
            CompatibilityConflictRow row
    ) {
        return IntakeResponse.CompatibilityConflict.builder()
                .combinationType(row.combinationType())
                .currentIngredientId(row.currentIngredientId())
                .currentIngredientName(row.currentIngredientName())
                .targetIngredientId(row.targetIngredientId())
                .targetIngredientName(row.targetIngredientName())
                .reason(row.reason())
                .build();
    }

    private static boolean isStreakIncluded(IntakeStreakStatus status) {
        return status == IntakeStreakStatus.COMPLETED || status == IntakeStreakStatus.MAINTAINED;
    }

    private static String toThumbnailImageUrl(
            ActiveProductRow row,
            Function<String, String> imageUrlResolver
    ) {
        return toThumbnailImageUrl(row.productId(), row.ingredientCount(), row.singleIngredientImageKey(), imageUrlResolver);
    }

    private static String toThumbnailImageUrl(
            Long productId,
            Long ingredientCount,
            String singleIngredientImageKey,
            Function<String, String> imageUrlResolver
    ) {
        String imageKey = ingredientCount != null && ingredientCount == 1L
                ? singleIngredientImageKey
                : EtcProductImageKeyResolver.resolve(productId);
        return imageUrlResolver.apply(imageKey);
    }

    private static Integer toIntakeDayCount(LocalDate startedOn, LocalDate currentDate) {
        if (startedOn == null || currentDate == null) {
            return null;
        }

        long dayCount = ChronoUnit.DAYS.between(startedOn, currentDate) + 1;
        return (int) Math.max(dayCount, 1);
    }

}
