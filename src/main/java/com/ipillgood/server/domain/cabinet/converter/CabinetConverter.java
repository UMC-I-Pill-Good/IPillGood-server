package com.ipillgood.server.domain.cabinet.converter;

import com.ipillgood.server.domain.cabinet.dto.CabinetResponse;
import com.ipillgood.server.domain.cabinet.entity.MemberProduct;
import com.ipillgood.server.domain.cabinet.repository.CabinetAddedProductRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductCandidateRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductDetailRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductIngredientKeywordRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductRow;
import com.ipillgood.server.domain.intake.entity.MemberActiveProduct;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CabinetConverter {

    private static final int MULTI_INGREDIENT_THUMBNAIL_MIN = 1;
    private static final int MULTI_INGREDIENT_THUMBNAIL_MAX = 4;
    private static final DateTimeFormatter INTAKE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private CabinetConverter() {
    }

    public static CabinetResponse.ProductCandidates toProductCandidates(
            String keyword,
            String sort,
            Integer page,
            Integer size,
            Long totalCount,
            Boolean hasNext,
            List<CabinetProductCandidateRow> rows,
            Map<Long, List<String>> tagsByProductId,
            String imageBaseUrl
    ) {
        List<CabinetResponse.ProductCandidate> products = rows.stream()
                .map(row -> toProductCandidate(row, tagsByProductId.getOrDefault(row.productId(), List.of()), imageBaseUrl))
                .toList();

        return CabinetResponse.ProductCandidates.builder()
                .keyword(keyword)
                .sort(sort)
                .page(page)
                .size(size)
                .totalCount(totalCount)
                .hasNext(hasNext)
                .products(products)
                .build();
    }

    public static CabinetResponse.ProductList toProductList(
            String memberNickname,
            List<CabinetProductRow> rows,
            String imageBaseUrl
    ) {
        List<CabinetResponse.ProductSummary> products = rows.stream()
                .map(row -> toProductSummary(row, imageBaseUrl))
                .toList();

        return CabinetResponse.ProductList.builder()
                .memberNickname(memberNickname)
                .totalCount(products.size())
                .products(products)
                .build();
    }

    public static CabinetResponse.AddProducts toAddProducts(
            List<CabinetAddedProductRow> rows,
            String imageBaseUrl
    ) {
        List<CabinetResponse.AddedProduct> addedProducts = rows.stream()
                .map(row -> toAddedProduct(row, imageBaseUrl))
                .toList();

        return CabinetResponse.AddProducts.builder()
                .addedCount(addedProducts.size())
                .addedProducts(addedProducts)
                .build();
    }

    public static CabinetResponse.DeleteProducts toDeleteProducts(
            List<MemberProduct> memberProducts,
            Map<Long, MemberActiveProduct> activeProductsByMemberProductId
    ) {
        List<CabinetResponse.DeletedProduct> deletedProducts = memberProducts.stream()
                .map(memberProduct -> toDeletedProduct(memberProduct, activeProductsByMemberProductId))
                .toList();

        return CabinetResponse.DeleteProducts.builder()
                .deletedCount(deletedProducts.size())
                .deletedProducts(deletedProducts)
                .build();
    }

    public static CabinetResponse.ProductDetail toProductDetail(
            CabinetProductDetailRow detailRow,
            List<CabinetProductIngredientKeywordRow> ingredientRows,
            LocalDate currentDate,
            String imageBaseUrl
    ) {
        Long activeProductId = detailRow.activeProductId();

        return CabinetResponse.ProductDetail.builder()
                .memberProductId(detailRow.memberProductId())
                .productId(detailRow.productId())
                .brand(detailRow.brand())
                .productName(detailRow.productName())
                .thumbnailImageUrl(toDetailThumbnailImageUrl(ingredientRows, imageBaseUrl))
                .isActiveIntake(activeProductId != null)
                .hasMyReview(detailRow.hasMyReview())
                .ingredients(toProductIngredients(ingredientRows, imageBaseUrl))
                .activeProduct(toActiveProduct(detailRow, currentDate))
                .build();
    }

    private static CabinetResponse.ProductCandidate toProductCandidate(
            CabinetProductCandidateRow row,
            List<String> ingredientTags,
            String imageBaseUrl
    ) {
        boolean isOwned = Boolean.TRUE.equals(row.isOwned());

        return CabinetResponse.ProductCandidate.builder()
                .productId(row.productId())
                .brand(row.brand())
                .productName(row.productName())
                .thumbnailImageUrl(toThumbnailImageUrl(row, imageBaseUrl))
                .averageRating(row.averageRating())
                .reviewCount(row.reviewCount() == null ? 0 : row.reviewCount().intValue())
                .ingredientTags(ingredientTags)
                .isOwned(isOwned)
                .isSelectable(!isOwned)
                .build();
    }

    private static CabinetResponse.DeletedProduct toDeletedProduct(
            MemberProduct memberProduct,
            Map<Long, MemberActiveProduct> activeProductsByMemberProductId
    ) {
        MemberActiveProduct activeProduct = activeProductsByMemberProductId.get(memberProduct.getId());
        Long stoppedActiveProductId = activeProduct == null ? null : activeProduct.getId();

        return CabinetResponse.DeletedProduct.builder()
                .memberProductId(memberProduct.getId())
                .productId(memberProduct.getProduct().getId())
                .productName(memberProduct.getProduct().getName())
                .wasActiveIntake(activeProduct != null)
                .stoppedActiveProductId(stoppedActiveProductId)
                .build();
    }

    private static CabinetResponse.ProductSummary toProductSummary(
            CabinetProductRow row,
            String imageBaseUrl
    ) {
        Long activeProductId = row.activeProductId();

        return CabinetResponse.ProductSummary.builder()
                .memberProductId(row.memberProductId())
                .productId(row.productId())
                .productName(row.productName())
                .thumbnailImageUrl(toThumbnailImageUrl(row, imageBaseUrl))
                .isActiveIntake(activeProductId != null)
                .activeProductId(activeProductId)
                .addedAt(row.addedAt())
                .build();
    }

    private static String toThumbnailImageUrl(CabinetProductRow row, String imageBaseUrl) {
        return toThumbnailImageUrl(row.ingredientCount(), row.singleIngredientImageKey(), imageBaseUrl);
    }

    private static String toThumbnailImageUrl(CabinetProductCandidateRow row, String imageBaseUrl) {
        return toThumbnailImageUrl(row.ingredientCount(), row.singleIngredientImageKey(), imageBaseUrl);
    }

    private static CabinetResponse.AddedProduct toAddedProduct(
            CabinetAddedProductRow row,
            String imageBaseUrl
    ) {
        return CabinetResponse.AddedProduct.builder()
                .memberProductId(row.memberProductId())
                .productId(row.productId())
                .brand(row.brand())
                .productName(row.productName())
                .thumbnailImageUrl(toThumbnailImageUrl(row, imageBaseUrl))
                .addedAt(row.addedAt())
                .build();
    }

    private static String toThumbnailImageUrl(CabinetAddedProductRow row, String imageBaseUrl) {
        return toThumbnailImageUrl(row.ingredientCount(), row.singleIngredientImageKey(), imageBaseUrl);
    }

    private static List<CabinetResponse.ProductIngredient> toProductIngredients(
            List<CabinetProductIngredientKeywordRow> rows,
            String imageBaseUrl
    ) {
        Map<Long, ProductIngredientAccumulator> ingredientsById = new LinkedHashMap<>();
        for (CabinetProductIngredientKeywordRow row : rows) {
            ProductIngredientAccumulator accumulator = ingredientsById.computeIfAbsent(
                    row.ingredientId(),
                    ingredientId -> new ProductIngredientAccumulator(
                            row.ingredientId(),
                            row.name(),
                            row.imageKey(),
                            row.description(),
                            new ArrayList<>()
                    )
            );

            if (row.effectKeyword() != null && !row.effectKeyword().isBlank()) {
                accumulator.effectTags().add(row.effectKeyword());
            }
        }

        return ingredientsById.values()
                .stream()
                .map(accumulator -> CabinetResponse.ProductIngredient.builder()
                        .ingredientId(accumulator.ingredientId())
                        .name(accumulator.name())
                        .imageUrl(toImageUrl(imageBaseUrl, accumulator.imageKey()))
                        .description(accumulator.description())
                        .effectTags(accumulator.effectTags())
                        .build())
                .toList();
    }

    private static CabinetResponse.ActiveProduct toActiveProduct(
            CabinetProductDetailRow row,
            LocalDate currentDate
    ) {
        if (row.activeProductId() == null) {
            return null;
        }

        return CabinetResponse.ActiveProduct.builder()
                .activeProductId(row.activeProductId())
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

    private static Integer toIntakeDayCount(LocalDate startedOn, LocalDate currentDate) {
        if (startedOn == null || currentDate == null) {
            return null;
        }

        long dayCount = ChronoUnit.DAYS.between(startedOn, currentDate) + 1;
        return (int) Math.max(dayCount, 1);
    }

    private static String toDetailThumbnailImageUrl(
            List<CabinetProductIngredientKeywordRow> rows,
            String imageBaseUrl
    ) {
        Map<Long, String> imageKeysByIngredientId = new LinkedHashMap<>();
        for (CabinetProductIngredientKeywordRow row : rows) {
            imageKeysByIngredientId.putIfAbsent(row.ingredientId(), row.imageKey());
        }

        String imageKey = imageKeysByIngredientId.size() == 1
                ? imageKeysByIngredientId.values().iterator().next()
                : randomMultiIngredientImageKey();
        return toImageUrl(imageBaseUrl, imageKey);
    }

    private static String toThumbnailImageUrl(Long ingredientCount, String singleIngredientImageKey, String imageBaseUrl) {
        String imageKey = ingredientCount != null && ingredientCount == 1L
                ? singleIngredientImageKey
                : randomMultiIngredientImageKey();
        return toImageUrl(imageBaseUrl, imageKey);
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

    private record ProductIngredientAccumulator(
            Long ingredientId,
            String name,
            String imageKey,
            String description,
            List<String> effectTags
    ) {
    }
}
