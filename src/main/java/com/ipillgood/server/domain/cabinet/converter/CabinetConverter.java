package com.ipillgood.server.domain.cabinet.converter;

import com.ipillgood.server.domain.cabinet.dto.CabinetResponse;
import com.ipillgood.server.domain.cabinet.repository.CabinetAddedProductRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductRow;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CabinetConverter {

    private static final int MULTI_INGREDIENT_THUMBNAIL_MIN = 1;
    private static final int MULTI_INGREDIENT_THUMBNAIL_MAX = 4;

    private CabinetConverter() {
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
}
