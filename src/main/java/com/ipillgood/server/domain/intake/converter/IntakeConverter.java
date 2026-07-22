package com.ipillgood.server.domain.intake.converter;

import com.ipillgood.server.domain.cabinet.entity.MemberProduct;
import com.ipillgood.server.domain.intake.dto.IntakeResponse;
import com.ipillgood.server.domain.intake.entity.MemberActiveProduct;
import com.ipillgood.server.domain.intake.repository.ActiveProductRow;
import com.ipillgood.server.domain.intake.repository.ActiveProductSettingsRow;
import com.ipillgood.server.domain.intake.repository.CompatibilityConflictRow;
import com.ipillgood.server.domain.product.entity.Product;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class IntakeConverter {

    private static final int MULTI_INGREDIENT_THUMBNAIL_MIN = 1;
    private static final int MULTI_INGREDIENT_THUMBNAIL_MAX = 4;
    private static final DateTimeFormatter INTAKE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private IntakeConverter() {
    }

    public static IntakeResponse.ActiveProducts toActiveProducts(
            List<ActiveProductRow> rows,
            String imageBaseUrl
    ) {
        List<IntakeResponse.ActiveProductSummary> activeProducts = rows.stream()
                .map(row -> toActiveProductSummary(row, imageBaseUrl))
                .toList();

        return IntakeResponse.ActiveProducts.builder()
                .totalCount(activeProducts.size())
                .activeProducts(activeProducts)
                .build();
    }

    public static IntakeResponse.RegisterActiveProduct toRegisterActiveProduct(
            MemberActiveProduct activeProduct,
            ActiveProductRow row,
            String imageBaseUrl
    ) {
        return IntakeResponse.RegisterActiveProduct.builder()
                .activeProductId(row.activeProductId())
                .memberProductId(row.memberProductId())
                .productId(row.productId())
                .productName(row.productName())
                .thumbnailImageUrl(toThumbnailImageUrl(row, imageBaseUrl))
                .notificationEnabled(activeProduct.isNotificationEnabled())
                .intakeTime(activeProduct.getIntakeTime().format(INTAKE_TIME_FORMATTER))
                .frequency(activeProduct.getFrequency().name())
                .frequencyLabel(activeProduct.getFrequency().getLabel())
                .build();
    }

    public static IntakeResponse.UpdateActiveProductSettings toUpdateActiveProductSettings(
            ActiveProductSettingsRow row,
            LocalDate currentDate,
            String imageBaseUrl
    ) {
        return IntakeResponse.UpdateActiveProductSettings.builder()
                .activeProductId(row.activeProductId())
                .memberProductId(row.memberProductId())
                .productId(row.productId())
                .brand(row.brand())
                .productName(row.productName())
                .thumbnailImageUrl(toThumbnailImageUrl(
                        row.ingredientCount(),
                        row.singleIngredientImageKey(),
                        imageBaseUrl
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
            String imageBaseUrl
    ) {
        return IntakeResponse.ActiveProductSummary.builder()
                .activeProductId(row.activeProductId())
                .memberProductId(row.memberProductId())
                .productId(row.productId())
                .productName(row.productName())
                .thumbnailImageUrl(toThumbnailImageUrl(row, imageBaseUrl))
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

    private static String toThumbnailImageUrl(ActiveProductRow row, String imageBaseUrl) {
        return toThumbnailImageUrl(row.ingredientCount(), row.singleIngredientImageKey(), imageBaseUrl);
    }

    private static String toThumbnailImageUrl(Long ingredientCount, String singleIngredientImageKey, String imageBaseUrl) {
        String imageKey = ingredientCount != null && ingredientCount == 1L
                ? singleIngredientImageKey
                : randomMultiIngredientImageKey();
        return toImageUrl(imageBaseUrl, imageKey);
    }

    private static Integer toIntakeDayCount(LocalDate startedOn, LocalDate currentDate) {
        if (startedOn == null || currentDate == null) {
            return null;
        }

        long dayCount = ChronoUnit.DAYS.between(startedOn, currentDate) + 1;
        return (int) Math.max(dayCount, 1);
    }

    private static String randomMultiIngredientImageKey() {
        int imageNumber = ThreadLocalRandom.current()
                .nextInt(MULTI_INGREDIENT_THUMBNAIL_MIN, MULTI_INGREDIENT_THUMBNAIL_MAX + 1);
        return "ingredients/other" + imageNumber + ".png";
    }

    private static String toImageUrl(String imageBaseUrl, String imageKey) {
        if (imageBaseUrl == null || imageBaseUrl.isBlank() || imageKey == null || imageKey.isBlank()) {
            return null;
        }

        String normalizedBaseUrl = imageBaseUrl.endsWith("/")
                ? imageBaseUrl.substring(0, imageBaseUrl.length() - 1)
                : imageBaseUrl;
        String normalizedImageKey = imageKey.startsWith("/")
                ? imageKey.substring(1)
                : imageKey;
        return normalizedBaseUrl + "/" + normalizedImageKey;
    }
}
