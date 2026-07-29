package com.ipillgood.server.domain.product.converter;

import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.product.dto.ProductResponse;
import com.ipillgood.server.domain.product.entity.Product;
import com.ipillgood.server.domain.review.dto.ProductReviewResponse;
import com.ipillgood.server.global.util.EtcProductImageKeyResolver;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ProductConverter {

    public static ProductResponse.ProductInfo toProductInfo(
            Product product,
            List<Ingredient> includedIngredients,
            ProductReviewResponse.ReviewSummary reviewSummary,
            Function<String, String> toImageUrl
    ) {
        String imageKey = includedIngredients.size() >= 2
                ? EtcProductImageKeyResolver.resolve(product.getId())
                : includedIngredients.get(0).getImageKey();

        List<String> adClaimRiskIngredients = extractAdClaimRiskIngredients(includedIngredients);

        return ProductResponse.ProductInfo.builder()
                .productId(product.getId())
                .productName(product.getName())
                .brand(product.getBrand())
                .imageUrl(toImageUrl.apply(imageKey))
                .description(product.getDescription())
                .purchaseUrl(product.getPurchaseUrl())
                .mfdsCertified(product.isMfdsCertified())
                .ratingAverage(reviewSummary.ratingAverage())
                .reviewCount(reviewSummary.reviewCount())
                .adClaimRisk(!adClaimRiskIngredients.isEmpty())
                .adClaimRiskIngredients(adClaimRiskIngredients)
                .build();
    }

    public static ProductResponse.ProductIngredientsInfo toProductIngredientsInfo(
            Long productId,
            List<Ingredient> ingredients,
            Map<Long, List<String>> ingredientEffectKeywords,
            Function<String, String> toImageUrl
    ) {

        List<ProductResponse.ProductIngredientsInfo.ProductIngredientInfo> ingredientInfos = ingredients.stream()
                .map(ingredient -> toIngredientInfo(
                        ingredient,
                        ingredientEffectKeywords.getOrDefault(ingredient.getId(), List.of()),
                        toImageUrl))
                .toList();

        return ProductResponse.ProductIngredientsInfo.builder()
                .productId(productId)
                .ingredientCount(ingredients.size())
                .ingredientInfos(ingredientInfos)
                .build();
    }

    public static ProductResponse.ProductCombinations toProductCombinations(
            Long productId,
            Long ownedProductCount,
            Set<Ingredient> goodIngredients,
            Set<Ingredient> cautionIngredients
    ) {
        return ProductResponse.ProductCombinations.builder()
                .productId(productId)
                .ownedProductCount(ownedProductCount)
                .goodCombinations(goodIngredients.stream()
                        .map(i -> ProductResponse.ProductCombinations.ProductCombination.builder()
                                .targetIngredientId(i.getId())
                                .targetIngredientName(i.getName())
                                .build())
                        .toList()
                )
                .cautionCombinations(cautionIngredients.stream()
                        .map(i -> ProductResponse.ProductCombinations.ProductCombination.builder()
                                .targetIngredientId(i.getId())
                                .targetIngredientName(i.getName())
                                .build())
                        .toList()
                )
                .build();
    }

    private static List<String> extractAdClaimRiskIngredients(List<Ingredient> ingredients) {
        return ingredients.stream()
                .filter(Ingredient::isAdClaimRisk)
                .map(Ingredient::getName)
                .toList();
    }

    private static ProductResponse.ProductIngredientsInfo.ProductIngredientInfo toIngredientInfo(
            Ingredient ingredient,
            List<String> effectKeywords,
            Function<String, String> toImageUrl
    ) {
        return ProductResponse.ProductIngredientsInfo.ProductIngredientInfo.builder()
                .ingredientId(ingredient.getId())
                .name(ingredient.getName())
                .description(ingredient.getDescription())
                .imageUrl(toImageUrl.apply(ingredient.getImageKey()))
                .effectKeywords(effectKeywords)
                .build();
    }
}
