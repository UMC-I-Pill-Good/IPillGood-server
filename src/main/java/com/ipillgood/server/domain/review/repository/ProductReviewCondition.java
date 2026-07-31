package com.ipillgood.server.domain.review.repository;

import com.ipillgood.server.domain.product.entity.Product;
import com.ipillgood.server.domain.review.entity.enums.ProductReviewSort;

import java.time.LocalDateTime;

public record ProductReviewCondition(
        Product product,
        ProductReviewSort sort,
        int size,
        Cursor cursor
) {

    public record Cursor(
            Long reviewId,
            LocalDateTime createdAt,
            Integer helpfulCount
    ) {
    }
}
