package com.ipillgood.server.domain.review.repository;

public class ProductReviewProjection {

    public record ReviewSummary(
            Double ratingAverage,
            Long reviewCount
    ) {}
}
