package com.ipillgood.server.domain.search.repository;


public final class ProductSearchProjection {

    public record Product(
            Long productId,
            String brand,
            String productName,
            Boolean mfdsCertified,
            Double averageRating,
            Long reviewCount
    ) {
    }

    public record Ingredient(
            Long productId,
            String ingredientName,
            String imageKey
    ) {
    }
}
